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

/**
 * Keeps track of all the internal state necessary to solve a [Cnf] formula.
 *
 * @property initialState The [Cnf] and the already assigned variables.
 * @property backtrackStack Keeps track of all currently applied [Action]s in reverse order.
 * @property binding Current binding used to try to solve the formula.
 * @property unboundVariablesInTerms Metadata to keep track how many unassigned variables every term currently has.
 * @property variablesToTerms Metadata for faster lookup which terms are affected by variable assignments.
 *
 */
class Solver(private val initialState: SolverState) {

    private val backtrackStack = Stack<Action>()
    private val binding = initialState.binding.copy()

    private val unboundVariablesInTerms = mutableMapOf<Cnf.Term, Int>()
    private val variablesToTerms: MutableMap<Variable, MutableList<Cnf.Term>> = mutableMapOf()

    /**
     * @constructor Computes initial value for metadata properties unboundVariablesInTerms and variablesToTerms.
     */
    init {
        for (term in initialState.cnf.terms) {
            unboundVariablesInTerms[term] = term.positiveVariables.count { !binding.binds(it) } +
                term.negativeVariables.count { !binding.binds(it) }
            term.positiveVariables.map { variablesToTerms.putIfAbsent(it, mutableListOf(term))?.add(term) }
            term.negativeVariables.map { variablesToTerms.putIfAbsent(it, mutableListOf(term))?.add(term) }
        }
    }

    /**
     * Helper function to undo the assignment a value to a variable and update all internal state.
     *
     * @param variable The variable that is unassigned.
     */
    private fun undoAssignment(variable: Variable) {
        binding.boundVariable.remove(variable)
        variablesToTerms[variable]?.map {
            unboundVariablesInTerms[it] = (unboundVariablesInTerms[it] ?: 0) + 1
        }
    }

    /**
     * Helper function to assign a value to a variable and update all internal state.
     *
     * @param variable The variable that is assigned a new value.
     * @param value The value that is assigned to the variable.
     */
    private fun assignVariable(variable: Variable, value: Boolean) {
        binding.boundVariable[variable] = value
        variablesToTerms[variable]?.map {
            unboundVariablesInTerms[it] = (unboundVariablesInTerms[it] ?: 1) - 1
        }
    }

    /**
     * Called if the cnf is unsatisfiable against the current binding.
     *
     * [Inference] actions are undone and the process is repeated. [Assignment] actions are checked, whether a
     * different value can be tried. If that's the case the new value is assigned and backtracking stops. If not
     * the variable assignment is undone and the process is repeated with the next action.
     *
     * @throws Unsatisfiable If the backtrackStack is empty, no more options can be tried. In this case the function
     * throws an Unsatisfiable exception.
     */
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

    /**
     * Finds variables whose value is completely determined by the current binding and the terms in the cnf.
     *
     * Searches all terms that contain the variable that was assigned a new value. If one of the terms does not
     * already evaluate to true and contains only one unassigned variable, this unassigned variable is assigned
     * a new value, such that the term evaluates to true.
     * This kind of assignment is tracked by [Inference] objects on the backtrackStack.
     *
     * @param variable This variable was assigned a new value. All checks are done against terms that contain
     * this variable.
     */
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

    /**
     * Tries to find a satisfying [Binding] for the [Cnf] in initialState.
     *
     * The algorithm iteratively assigns new values to variables and tries to infer as many variables as
     * possible. If the formula is unsatisfiable against the current binding, it undoes as many last [Action]s
     * as necessary until a new path can be tried.
     * This kind of assignment is tracked by [Assignment] objects on the backtrackStack.
     *
     * @return A [Binding] which evaluates the [Cnf] to [EvaluationResult.TRUE].
     * @throws Unsatisfiable, iff there is no binding that satisfies the cnf formula.
     */
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
