package de.fuchsch.satsolver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SolverUnitTest {

    @Test
    fun `Solving a single variable term works`() {
        val variable = Variable.create()
        val knf = Cnf(mutableSetOf(variable))
        knf += variable
        val solution = solve(SolverState(knf, Binding()))
        assertEquals(EvaluationResult.TRUE, knf.evaluate(solution))
    }

    @Test
    fun `Solving with multiple variables works`() {
        val variables = Array(5) { Variable.create() }
        val knf = Cnf(variables.toMutableSet())
        knf += variables[0] - variables[1]
        knf += variables[2] + variables[3] + variables[4]
        val solution = solve(SolverState(knf, Binding()))
        assertEquals(EvaluationResult.TRUE, knf.evaluate(solution))
    }

    @Test
    fun `Solving a equation that enforces backtracking`() {
        val variables = Array(5) { Variable.create() }
        val knf = Cnf(variables.toMutableSet())
        knf += variables[0] - variables[1]
        knf += variables[2]
        knf += variables[0] - variables[3]
        knf += variables[0] - variables[4]
        knf += variables[3] + variables[4]
        val solution = solve(SolverState(knf, Binding()))
        assertEquals(EvaluationResult.TRUE, knf.evaluate(solution))
    }

    @Test
    fun `Solving a 20 variable equation`() {
        val variables = Array(20) { Variable.create() }
        val knf = Cnf(variables.toMutableSet())
        for (i in 0..18) {
            knf += variables[i] - variables[18]
            knf += variables[i] - variables[19]
        }
        knf += variables[18] + variables[19]
        val solution = solve(SolverState(knf, Binding()))
        assertEquals(EvaluationResult.TRUE, knf.evaluate(solution))
    }
}