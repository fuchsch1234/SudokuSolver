package de.fuchsch.satsolver

data class SolverState(
    val knf: Knf,
    val binding: Binding
)

fun solve(initialState: SolverState): Binding {
    val binding = initialState.binding
    return binding
}