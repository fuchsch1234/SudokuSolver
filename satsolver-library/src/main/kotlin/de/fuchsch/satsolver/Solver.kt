package de.fuchsch.satsolver

import java.util.*

internal sealed class Action

internal data class Assignment(val variable: Variable): Action()

internal data class Inference(val variable: Variable): Action()

class Unsatisfiable(what: String): Error(what)

data class SolverState(
    val knf: Knf,
    val binding: Binding
)

class Solver(private val initialState: SolverState) {

    private val backtrackStack = Stack<Action>()
    private val binding = initialState.binding.copy()

    private val unboundVariablesInTerms = mutableMapOf<Knf.Term, Int>()
    private val variablesToTerms: MutableMap<Variable, MutableList<Knf.Term>> = mutableMapOf()

    init {
        for (term in initialState.knf.terms) {
            unboundVariablesInTerms[term] = term.positiveVariables.count { !binding.boundVariable.containsKey(it) } +
                term.negativeVariables.count { !binding.boundVariable.containsKey(it) }
            term.positiveVariables.map { variablesToTerms.putIfAbsent(it, mutableListOf(term))?.add(term) }
            term.negativeVariables.map { variablesToTerms.putIfAbsent(it, mutableListOf(term))?.add(term) }
        }
    }

    private fun undoAssignment(variable: Variable) {
        binding.boundVariable.remove(variable)
        variablesToTerms[variable]?.map {
            unboundVariablesInTerms[it] = (unboundVariablesInTerms[it] ?: 0) + 1
        }
        backtrackStack.pop()
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
            if (backtrackStack.isEmpty()) throw Unsatisfiable("Equation cannot be satisfied")
            action = backtrackStack.peek()
        }
    }

    internal fun solve(): Binding {
        val knf = initialState.knf
        while (true) {
            when (knf.evaluate(binding)) {
                EvaluationResult.TRUE -> return binding
                EvaluationResult.FALSE -> backtrack()
                else -> {
                    val variable = knf.variables.first {  !binding.boundVariable.containsKey(it)  }
                    binding.boundVariable[variable] = false
                    variablesToTerms[variable]?.map {
                        unboundVariablesInTerms[it] = (unboundVariablesInTerms[it] ?: 1) - 1
                    }
                    backtrackStack.push(Assignment(variable))
                }
            }
        }
    }

}

fun solve(initialState: SolverState): Binding = Solver(initialState).solve()
