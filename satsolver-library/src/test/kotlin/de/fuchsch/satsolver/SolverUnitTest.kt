package de.fuchsch.satsolver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SolverUnitTest {

    @Test
    fun `Solving a single variable term works`() {
        val literal = Literal.Positive(Variable.create())
        val clause = listOf(CnfTerm(literal))
        val solution = solve(clause)
        assertEquals(EvaluationResult.TRUE, clause.evaluate(solution))
    }

    @Test
    fun `Solving with multiple literals works`() {
        val literals = Array(5) { Literal.Positive(Variable.create()) }
        val clause = listOf(literals[0] / literals[1]) +
                (literals[2] / !literals[4]) +
                (literals[1] / !literals[2]) +
                (!literals[0] / literals[3]) +
                CnfTerm(!literals[3])
        val binding = Binding()
        val solution = solve(clause, binding)
        assertEquals(EvaluationResult.TRUE, clause.evaluate(solution))
        assertEquals(false, solution[literals[0]])
        assertEquals(true, solution[literals[1]])
        assertEquals(false, solution[literals[3]])
        assertEquals(false, solution[literals[4]])
    }

    @Test
    fun `Solving a equation that enforces backtracking`() {
        val literals = Array(5) { Literal.Positive(Variable.create()) }
        val clause = listOf(literals[0] / !literals[1]) +
                CnfTerm(literals[2]) +
                CnfTerm(!literals[1]) +
                (literals[0] / literals[3]) +
                (literals[0] / !literals[4]) +
                (literals[3] / literals[4])
        val solution = solve(clause)
        assertEquals(EvaluationResult.TRUE, clause.evaluate(solution))
    }

    @Test
    fun `Solving a 20 variable equation`() {
        val literals = Array(20) { Literal.Positive(Variable.create()) }
        val knf = emptyList<CnfTerm>().toMutableList()
        for (i in 0..17) {
            knf += literals[i] / !literals[18]
            knf += literals[i] / !literals[19]
        }
        knf += literals[18] / literals[19]
        val solution = solve(knf)
        assertEquals(EvaluationResult.TRUE, knf.evaluate(solution))
    }

    @Test
    fun `Solving a unsolvable equation produces correct result`() {
        val variable = Variable.create()
        val cnf = listOf(CnfTerm(Literal.Positive(variable)), CnfTerm(Literal.Negation(variable)))
        val solution = solve(cnf)
        assertEquals(EvaluationResult.FALSE, cnf.evaluate(solution))
    }

    @Test
    fun `Solving for exactly one variable works`() {
        val variables = Array(4) { Variable.create() }
        val clause = exactlyOneOf(variables.toList())
        val solution = solve(clause)
        assertEquals(EvaluationResult.TRUE, clause.evaluate(solution))
    }

    @Test
    fun `Solving for exactly one variable with constraints works`() {
        val variables = Array(4) { Variable.create() }
        var clause = exactlyOneOf(listOf(variables[0], variables[1], variables[2], variables[3]))
        clause += exactlyOneOf(listOf(variables[2], variables[3]))
        val solution = solve(clause)
        assertEquals(EvaluationResult.TRUE, clause.evaluate(solution))
        assertEquals(false, solution[variables[0]])
        assertEquals(false, solution[variables[1]])
        assertEquals(true, solution[variables[2]])
        assertEquals(false, solution[variables[3]])
    }
}