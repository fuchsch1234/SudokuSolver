package de.fuchsch.satsolver

import java.util.*

/**
 * Model for the different actions the [Solver] takes in order to find a satisfying [Binding].
 *
 * Internally the solver uses backtracking to search for a satisfying [Binding]. To try different
 * paths the algorithm must be able to revert previous actions and try different ones. The subclasses
 * of this class are used to keep track which actions the algorithm already tried.
 */
internal sealed class Action

/**
 * Model for assignment of a variable to a boolean value.
 *
 * @property literal The variable that is assigned in a step of the algorithm.
 */
internal data class Assignment(val literal: Literal): Action()

/**
 * Exception that is thrown if there exists no binding, satisfying the given
 * boundary conditions, that evaluates a [Clause] to true.
 *
 * @param what Message describing the exception.
 */
class Unsatisfiable(what: String): Error(what)

/**
 * Keeps track of all the internal state necessary to solve a [Clause].
 *
 * @property backtrackStack Keeps track of all currently applied [Action]s in reverse order.
 *
 */
class Solver(private val clause: Clause, private val binding: Binding) {

    private val backtrackStack = Stack<Action>()
    private val unboundVariables: MutableSet<Literal> = mutableSetOf()

    init {
        analyze(clause)
    }

    private fun analyze(clause: Clause) {
        when (clause) {
            is Literal -> if (!binding.binds(clause)) unboundVariables.add(clause)
            is Negation -> if (!binding.binds(clause.variable)) unboundVariables.add(clause.variable)
            is OrClause -> clause.clauses.map { analyze(it) }
            is AndClause -> clause.clauses.map { analyze(it) }
        }
    }

    /**
     * Tries to find a satisfying [Binding] for the [Clause] in initialState.
     *
     * The algorithm iteratively assigns new values to variables and tries to infer as many variables as
     * possible. If the formula is unsatisfiable against the current binding, it undoes as many last [Action]s
     * as necessary until a new path can be tried.
     * This kind of assignment is tracked by [Assignment] objects on the backtrackStack.
     *
     * @return A [Binding] which evaluates the [Clause] to [EvaluationResult.TRUE].
     * @throws Unsatisfiable, iff there is no binding that satisfies the cnf formula.
     */
    internal fun solve(): Binding {
        throw NotImplementedError()
    }

}

/**
 * Searches for a [Binding] that satisfies a [Clause], i.e. evaluates it to [EvaluationResult.TRUE]
 *
 * @return A [Binding] that satisfies the given cnf with the preset variables assignments.
 * @throws Unsatisfiable If there is no [Binding] satisfying the cnf.
 */
fun solve(clause: Clause, binding: Binding = Binding()): Binding = Solver(clause, binding.deepCopy()).solve()
