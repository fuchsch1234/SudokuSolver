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

    private fun backtrack() {
        var action = backtrackStack.peek()
        loop@ while (action != null) {
            when (action) {
                is Assignment -> {
                    if (binding.boundVariable[action.variable] == false) {
                        binding.boundVariable[action.variable] = true
                        break@loop
                    } else {
                        binding.boundVariable.remove(action.variable)
                        backtrackStack.pop()
                    }
                }
                is Inference -> {
                    binding.boundVariable.remove(action.variable)
                    backtrackStack.pop()
                }
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
                    backtrackStack.push(Assignment(variable))
                }
            }
        }
    }

}

fun solve(initialState: SolverState): Binding = Solver(initialState).solve()
