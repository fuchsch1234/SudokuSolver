package de.fuchsch.satsolver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SolverUnitTest {

    @Test
    fun `Solving a single variable term works`() {
        val variable = Variable.create()
        val knf = Knf(mutableListOf(variable))
        knf += variable
        val solution = solve(SolverState(knf, Binding()))
        assertEquals(EvaluationResult.TRUE, knf.evaluate(solution))
    }

    @Test
    fun `Solving with multiple variables works`() {
        val variables = Array(5) { Variable.create() }
        val knf = Knf(variables.toMutableList())
        knf += variables[0] - variables[1]
        knf += variables[2] + variables[3] + variables[4]
        val solution = solve(SolverState(knf, Binding()))
        assertEquals(EvaluationResult.TRUE, knf.evaluate(solution))
    }
}