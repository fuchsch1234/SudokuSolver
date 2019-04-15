package de.fuchsch.satsolver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClauseUnitTest {

    @Test
    fun `Evaluating a single literal works`() {
        val variable = Variable.create()
        val literal = Literal.Positive(variable)
        val binding = Binding()
        assert(literal.evaluate(binding) == EvaluationResult.UNDEFINED)
        binding[literal] = false
        assert(literal.evaluate(binding) == EvaluationResult.FALSE)
        binding[literal] = true
        assert(literal.evaluate(binding) == EvaluationResult.TRUE)
    }

    @Test
    fun `Empty OrClause evaluates false`() {
        val clause = CnfTerm.empty()
        val binding = Binding()
        assertEquals(0, clause.size)
        assert(clause.evaluate(binding) == EvaluationResult.FALSE)
    }

    @Test
    fun `Empty AndClause evaluates true`() {
        val clause = emptyList<CnfTerm>()
        val binding = Binding()
        assertEquals(0, clause.size)
        assert(clause.evaluate(binding) == EvaluationResult.TRUE)
    }

    @Test
    fun `OrClause evaluates correctly`() {
        val literals = Array(2) { Literal.Positive(Variable.create()) }
        val clause = !literals[0] / !literals[1]
        val binding = Binding()
        binding[literals[0]] = true
        binding[literals[1]] = false
        assertEquals(2, clause.size)
        assert(clause.evaluate(binding) == EvaluationResult.TRUE)
    }

    @Test
    fun `AndClause evaluates correctly`() {
        val literals = Array(2) { Literal.Positive(Variable.create()) }
        val clause = listOf(CnfTerm(literals[0]), CnfTerm(!literals[1]))
        val binding = Binding()
        binding[literals[0]] = true
        binding[literals[1]] = false
        assertEquals(2, clause.size)
        assert(clause.evaluate(binding) == EvaluationResult.TRUE)
    }

}

