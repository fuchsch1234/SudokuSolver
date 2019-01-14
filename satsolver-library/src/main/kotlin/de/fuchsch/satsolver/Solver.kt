package de.fuchsch.satsolver

import java.util.*

internal sealed class Action {

    abstract fun tryNext(binding: Binding): Boolean

}

internal class Assignment(private val variable: Variable): Action() {

    val iter = options.iterator()

    override fun tryNext(binding: Binding): Boolean = when(iter.hasNext()) {
        false -> {
            binding.boundVariable.remove(variable)
            false
        }
        true -> {
            binding.boundVariable[variable] = iter.next()
            true
        }
    }

    companion object {

        internal val options = listOf(false, true)

    }

}

internal class Inference(private val variable: Variable, private val value: Boolean): Action() {

    override fun tryNext(binding: Binding): Boolean {
        binding.boundVariable.remove(variable)
        return false
    }

}

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
        while (!action.tryNext(binding)) {
            backtrackStack.pop()
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
                    val assignment = Assignment(variable)
                    assignment.tryNext(binding)
                    backtrackStack.push(assignment)
                }
            }
        }
    }

}

fun solve(initialState: SolverState): Binding = Solver(initialState).solve()
