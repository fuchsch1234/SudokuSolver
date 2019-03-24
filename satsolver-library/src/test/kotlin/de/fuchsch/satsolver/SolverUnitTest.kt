package de.fuchsch.satsolver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SolverUnitTest {

    @Test
    fun `Solving a single variable term works`() {
        val literal = Literal.Positive(Variable.create())
        val solution = solve(literal)
        assertEquals(EvaluationResult.TRUE, literal.evaluate(solution))
    }

    @Test
    fun `Solving with multiple literals works`() {
        val literals = Array(5) { Literal.Positive(Variable.create()) }
        val clause = (literals[0] / literals[1]) + (literals[2] / !literals[4]) + (literals[1] / !literals[2])
        val binding = Binding()
        binding[literals[1]] = false
        val solution = solve(clause, binding)
        assertEquals(EvaluationResult.TRUE, clause.evaluate(solution))
        assertEquals(EvaluationResult.TRUE, solution[literals[0]])
        assertEquals(EvaluationResult.FALSE, solution[literals[1]])
        assertEquals(EvaluationResult.FALSE, solution[literals[2]])
        assertEquals(EvaluationResult.FALSE, solution[literals[4]])
    }

    @Test
    fun `Solving a equation that enforces backtracking`() {
        val literals = Array(5) { Literal.Positive(Variable.create()) }
        val clause = (literals[0] / !literals[1]) +
                literals[2] +
                !literals[1] +
                (literals[0] / literals[3]) +
                (literals[0] / !literals[4]) +
                (literals[3] / literals[4])
        val solution = solve(clause)
        assertEquals(EvaluationResult.TRUE, clause.evaluate(solution))
    }

    @Test
    fun `Solving a 20 variable equation`() {
        val literals = Array(20) { Literal.Positive(Variable.create()) }
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
        val literals = Array(4) { Literal.Positive(Variable.create()) }
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