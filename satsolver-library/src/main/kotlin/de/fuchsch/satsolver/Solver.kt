package de.fuchsch.satsolver

import arrow.core.*
import arrow.optics.extensions.*
import arrow.optics.optics
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/**
 * Exception that is thrown if there exists no binding, satisfying the given
 * boundary conditions, that evaluates a [Cnf] to true.
 *
 * @param what Message describing the exception.
 */
class Unsatisfiable(what: String): Error(what)

/**
 * Class used for finding satisfying [Binding]s for [Cnf]s.
 *
 * @property binding Initial binding constraints for the formula.
 * @property initialState Initial internal solver state.
 */
class Solver(formula: List<CnfTerm>, private val binding: Binding) {

    /**
     * Immutable state used during solving of [Cnf].
     *
     * @property formula Current state of the formula with all terms that have not been solved yet.
     * @property metaMapping Backwards mapping from [Literal]s to their containing [CnfTerm]s.
     */
    @optics data class SolverState(val formula: List<CnfTerm>, val metaMapping: Map<Literal, List<CnfTerm>>) {
        /**
         * Helper classes for working with immutable [SolverState] objects.
         */
        private fun mappingAt(literal: Literal) = MapAt<Literal, List<CnfTerm>>().at(literal)

        operator fun plus(literal: Literal) = SolverState(
            formula + CnfTerm(literal),
                    mappingAt(literal).modify(metaMapping) { it.map { it + CnfTerm(literal) } })
        companion object
    }

    private val initialState: SolverState
    private val scope = newFixedThreadPoolContext(4, "Pool")

    init {
        // Construct the initial solver state from the supplied formula
        val meta = reverseMapping(formula)
        initialState = SolverState(formula, meta)
    }

    private fun reverseMapping(formula: List<CnfTerm>): Map<Literal, List<CnfTerm>> =
        runBlocking(scope) {
            val meta = mutableMapOf<Literal, List<CnfTerm>>()
            if (formula.isEmpty()) {
                meta
            } else {
                val producers =
                    formula.chunked(maxOf(formula.size / 4, 4)).map {
                        produce {
                            val meta = mutableMapOf<Literal, List<CnfTerm>>()
                            it.forEach { term ->
                                term.literals.forEach { literal ->
                                    meta[literal] = meta.getOrDefault(literal, emptyList()) + term
                                }
                            }
                            send(meta)
                        }
                    }
                for (producer in producers) {
                    producer.receive().map { (key, value) ->
                        meta[key] = meta.getOrDefault(key, emptyList()) + value
                    }
                }
                meta
            }
        }

    /**
     * Tries to find a satisfying [Binding] for the [Cnf] in initialState.
     *
     * @return A [Binding] which evaluates the [Cnf] to [EvaluationResult.TRUE].
     * @throws Unsatisfiable, iff there is no binding that satisfies the cnf formula.
     */
    internal fun solve(): Binding =
        when (dpll(initialState)) {
            is Either.Left<Unsatisfiable> -> throw Unsatisfiable("Formula is unsatisfiable")
            is Either.Right<Boolean> -> binding
        }

    /**
     * Unit literals are literals that appear in terms that contain only one literal.
     */
    private fun unitLiterals(clauses: List<CnfTerm>): List<Literal> =
        clauses.filter { it.size == 1 }.map { it.literals.first() }

    /**
     * Pure literals are literals that appear with the same polarity (positive/negation) in all terms.
     */
    private fun isPureLiteral(state: SolverState, literal: Literal): Boolean =
        !state.metaMapping[literal].isNullOrEmpty() && state.metaMapping[!literal].isNullOrEmpty()

    /**
     * Finds all [pure literals][pureLiterals] in the current state.
     *
     * @param state The solver state to search for pure literals.
     * @return A list of all pure literals in the supplied state.
     */
    private fun pureLiterals(state: SolverState): List<Literal> =
        state.metaMapping.filter { (key, _) -> isPureLiteral(state, key) }.keys.toList()

    /**
     * Binds the supplied variables according to their polarity.
     *
     * @param _state The initial solver state.
     * @param literals The variables will be bound such that these literals evaluate to true.
     * @return A new solver state where all terms containing these literals and all occurrences of negated literals
     * have been removed.
     */
    private fun assignLiterals(_state: SolverState, literals: List<Literal>): SolverState {
        literals.forEach { literal ->
            when (literal) {
                is Literal.Negation -> binding[literal] = false
                is Literal.Positive -> binding[literal] = true
            }
        }

        if (_state.formula.isEmpty()) {
            return SolverState(emptyList(), emptyMap())
        }

        val terms = literals.flatMap { _state.metaMapping.getOrDefault(it, emptyList()) }
        val negatedLiterals = literals.map { !it }
        val formula = runBlocking(scope) {
            val formula = mutableListOf<CnfTerm>()
            val producers =
                _state.formula.chunked(maxOf(_state.formula.size / 4, 4)).map {
                    produce {
                        val f = mutableListOf<CnfTerm>()
                        it.forEach { term ->
                            if (!(term in terms)) {
                                f.add(CnfTerm.literals.modify(term) { it - negatedLiterals })

                            }
                        }
                        send(f)
                    }
                }
            for (producer in producers) {
                formula.addAll(producer.receive())
            }
            formula
        }
        return SolverState(formula, reverseMapping(formula))
    }

    /**
     * Simplifies a formula by finding all unit and pure literals, binding their variables and removing terms and
     * literals from the formula.
     *
     * @param _state A solver state that can be simplified.
     * @return The simplified state.
     */
    private fun simplify(_state: SolverState): Either<Unsatisfiable, SolverState> {
        var state = _state
        var oldState = SolverState(emptyList(), emptyMap())
        // As long as unit or pure literals are found continue simplifying, because
        // after assigning pure literals new unit literals may be found and vice versa.
        while (oldState != state) {
            oldState = state
            val units = unitLiterals(state.formula)
            if (units.any { literal -> units.contains(!literal)}) return Unsatisfiable("Cannot satisfy").left()
            state = assignLiterals(state, units)
            val pures = pureLiterals(state)
            state = assignLiterals(state, pures)
            logger.debug("Removed ${units.size} unit terms and ${pures.size} pure literals")
        }
        return state.right()
    }

    /**
     * Finds a satisfying binding for a formula.
     *
     * First step is to simplify the formula by removing all unit and pure literals. Then the formula is checked,
     * whether it is already solved or in an unsolvable state. If not a unbound literal is assigned both
     * possible values and the resulting formula is checked recursively.
     *
     * @_state The initial state containing the formula to solve.
     * @return True iff there is a satisfying binding for the formula, else False.
     */
    private fun dpll(_state: SolverState): Either<Unsatisfiable, Boolean> {
        logger.debug("Formula has ${_state.formula.size} terms and ${_state.metaMapping.size} unbound variables")
        return simplify(_state).map { state ->
            logger.debug("Simplified formula: ${state.formula}")
            // Check if formula is satisfied
            if (state.formula.isEmpty()) return true.right()
            // Check if formula is unsatisfiable
            if (state.formula.any { it.size == 0 }) return Unsatisfiable("Unsatisfiable").left()

            // Assign next unbound literal by adding a unit clause
            val nextLiteral = state.metaMapping.keys.first { !state.metaMapping[it].isNullOrEmpty() }
            return dpll(state + nextLiteral).handleErrorWith { dpll(state + !nextLiteral) }
        }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(this::class.java.enclosingClass.name)

    }

}

/**
 * Searches for a [Binding] that satisfies a [Clause], i.e. evaluates it to [EvaluationResult.TRUE]
 *
 * @return A [Binding] that satisfies the given cnf with the preset variables assignments.
 * @throws Unsatisfiable If there is no [Binding] satisfying the cnf.
 */
fun solve(clause: List<CnfTerm>, binding: Binding = Binding()): Binding = Solver(clause, binding.deepCopy()).solve()
