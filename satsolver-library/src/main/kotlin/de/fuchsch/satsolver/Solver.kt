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
 * @property variable The variable that is assigned in a step of the algorithm.
 */
internal data class Assignment(val variable: Variable): Action()

/**
 * Model for assignment of a value to a variable, because the value could be inferred from a term
 * and a specific binding, i.e. because a term evaluates to [EvaluationResult.UNDEFINED] and
 * contains only one unassigned variable.
 *
 * @property variable The variable that is assigned in a step of the algorithm.
 */
internal data class Inference(val variable: Variable): Action()

/**
 * Exception that is thrown if there exists no binding, satisfying the given
 * boundary conditions, that evaluates a [Cnf] to true.
 *
 * @property what Message describing the exception.
 */
class Unsatisfiable(what: String): Error(what)

/**
 * Keeps track of the [Solver]s internal state.
 *
 * @property cnf The [Cnf] the solver tries to satisfy.
 * @property binding The [Binding] currently used to evaluate cnf.
 */
data class SolverState(
    val cnf: Cnf,
    val binding: Binding
)

class Solver(private val initialState: SolverState) {

    private val backtrackStack = Stack<Action>()
    private val binding = initialState.binding.copy()

    private val unboundVariablesInTerms = mutableMapOf<Cnf.Term, Int>()
    private val variablesToTerms: MutableMap<Variable, MutableList<Cnf.Term>> = mutableMapOf()

    init {
        for (term in initialState.cnf.terms) {
            unboundVariablesInTerms[term] = term.positiveVariables.count { !binding.binds(it) } +
                term.negativeVariables.count { !binding.binds(it) }
            term.positiveVariables.map { variablesToTerms.putIfAbsent(it, mutableListOf(term))?.add(term) }
            term.negativeVariables.map { variablesToTerms.putIfAbsent(it, mutableListOf(term))?.add(term) }
        }
    }

    private fun undoAssignment(variable: Variable) {
        binding.boundVariable.remove(variable)
        variablesToTerms[variable]?.map {
            unboundVariablesInTerms[it] = (unboundVariablesInTerms[it] ?: 0) + 1
        }
    }

    private fun assignVariable(variable: Variable, value: Boolean) {
        binding.boundVariable[variable] = value
        variablesToTerms[variable]?.map {
            unboundVariablesInTerms[it] = (unboundVariablesInTerms[it] ?: 1) - 1
        }
    }

    private fun backtrack() {
        var action = backtrackStack.peek()
        loop@ while (action != null) {
            when (action) {
                is Assignment ->
                    if (binding.boundVariable[action.variable] == false) {
                        binding.boundVariable[action.variable] = true
                        break@loop
                    } else {
                        undoAssignment(action.variable)
                    }
                is Inference -> undoAssignment(action.variable)
            }
            backtrackStack.pop()
            if (backtrackStack.isEmpty()) throw Unsatisfiable("Equation cannot be satisfied")
            action = backtrackStack.peek()
        }
    }

    private fun inferVariablesFrom(variable: Variable) {
        variablesToTerms[variable]?.map {
            when (it.evaluate(binding)) {
                EvaluationResult.UNDEFINED -> if (unboundVariablesInTerms[it] == 1) {
                    val unbound = it.positiveVariables.find { !binding.binds(it) } ?:
                        it.negativeVariables.find { !binding.binds(it) }
                    if (unbound != null) {
                        if (it.positiveVariables.contains(unbound)) {
                            assignVariable(unbound, true)
                        } else {
                            assignVariable(unbound, false)
                        }
                        backtrackStack.push(Inference(unbound))
                    }
                }
                else -> { }
            }
        }
    }

    internal fun solve(): Binding {
        val knf = initialState.cnf
        while (true) {
            when (knf.evaluate(binding)) {
                EvaluationResult.TRUE -> return binding
                EvaluationResult.FALSE -> backtrack()
                else -> {
                    val variable = knf.variables.first {  !binding.boundVariable.containsKey(it)  }
                    assignVariable(variable, false)
                    backtrackStack.push(Assignment(variable))
                    inferVariablesFrom(variable)
                }
            }
        }
    }

}

/**
 * Searches for a [Binding] that satisfies a [Cnf], i.e. evaluates it to [EvaluationResult.TRUE]
 *
 * @param initialState The initial state to use. Contains the boundary conditions
 * in the form of preset variable assignments.
 * @return A [Binding] that satisfies the given cnf with the preset variables assignments.
 * @throws Unsatisfiable If there is no [Binding] satisfying the cnf.
 */
fun solve(initialState: SolverState): Binding = Solver(initialState).solve()
