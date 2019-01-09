package de.fuchsch.satsolver

import java.util.*

data class SolverState(
    val knf: Knf,
    val binding: Binding
)

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

class Unsatisfiable(what: String): Error(what)

internal fun backtrack(backtrackStack: Stack<Action>, binding: Binding) {
    var action = backtrackStack.peek()
    while (!action.tryNext(binding)) {
        backtrackStack.pop()
        if (backtrackStack.isEmpty()) throw Unsatisfiable("Equation cannot be satisfied")
        action = backtrackStack.peek()
    }
}

fun solve(initialState: SolverState): Binding {
    val knf = initialState.knf
    val binding = initialState.binding.copy()
    val backtrackStack = Stack<Action>()
    while (true) {
        when (knf.evaluate(binding)) {
            EvaluationResult.TRUE -> return binding
            EvaluationResult.FALSE -> backtrack(backtrackStack, binding)
            else -> {
                val variable = knf.variables.first {  !binding.boundVariable.containsKey(it)  }
                val assignment = Assignment(variable)
                assignment.tryNext(binding)
                backtrackStack.push(assignment)
            }
        }
    }
}