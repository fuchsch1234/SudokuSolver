package de.fuchsch.satsolver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SolverUnitTest {

    @Test
    fun `Solving a single variable term works`() {
        val literal = Literal.create()
        val solution = solve(literal)
        assertEquals(true, solution.evaluate(literal))
    }

    @Test
    fun `Solving with multiple literals works`() {
        val literals = Array(5) { Literal.create() }
        val clause = (literals[0] / literals[1]) + (literals[2] / !literals[4]) + (literals[1] / !literals[2])
        val binding = Binding()
        binding[literals[1]] = false
        val solution = solve(clause, binding)
        assertEquals(true, clause.evaluate(solution))
        assertEquals(true, solution[literals[0]])
        assertEquals(false, solution[literals[1]])
        assertEquals(false, solution[literals[2]])
        assertEquals(false, solution[literals[4]])
    }

    @Test
    fun `Solving a equation that enforces backtracking`() {
        val literals = Array(5) { Literal.create() }
        val clause = (literals[0] / !literals[1]) +
                literals[2] +
                (literals[0] / literals[3]) +
                (literals[0] / !literals[4]) +
                (literals[3] / literals[4])
        val solution = solve(clause)
        assertEquals(true, clause.evaluate(solution))
    }

    @Test
    fun `Solving a 20 variable equation`() {
        val literals = Array(20) { Literal.create() }
        /*
        val knf = Cnf(literals.toMutableSet())
        for (i in 0..18) {
            knf += literals[i] - literals[18]
            knf += literals[i] - literals[19]
        }
        knf += literals[18] + literals[19]
        val solution = solve()
        assertEquals(EvaluationResult.TRUE, knf.evaluate(solution))
        */
    }

    @Test
    fun `Solving a Dnf works`() {
        val literals = Array(4) { Literal.create() }
        /*
        val dnf = Dnf(literals.toMutableSet())
        dnf += Dnf.Term() + literals[0] - literals[1] - literals[2]
        dnf += Dnf.Term() - literals[0] + literals[1] - literals[2]
        dnf += Dnf.Term() - literals[0] - literals[1] + literals[2]
        val cnf = dnf.toCnf()
        val solution = solve()
        assertEquals(EvaluationResult.TRUE, dnf.evaluate(solution))
        */
    }
}