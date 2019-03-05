package de.fuchsch.satsolver

import org.junit.jupiter.api.Test

class ClauseUnitTest {

    @Test
    fun `Evaluating a single literal works`() {
        val literal = Literal.create()
        val binding = Binding()
        assert(literal.evaluate(binding) == EvaluationResult.UNDEFINED)
        binding[literal] = false
        assert(literal.evaluate(binding) == EvaluationResult.FALSE)
        binding[literal] = true
        assert(literal.evaluate(binding) == EvaluationResult.TRUE)
    }

    @Test
    fun `Empty OrClause evaluates false`() {
        val clause = OrClause(emptyList())
        val binding = Binding()
        assert(clause.evaluate(binding) == EvaluationResult.FALSE)
    }

    @Test
    fun `Empty AndClause evaluates true`() {
        val clause = AndClause(emptyList())
        val binding = Binding()
        assert(clause.evaluate(binding) == EvaluationResult.TRUE)
    }

    @Test
    fun `OrClause evaluates correctly`() {
        val literals = Array(2) { Literal.create() }
        val clause = !literals[0] / !literals[1]
        val binding = Binding()
        binding[literals[0]] = true
        binding[literals[1]] = false
        assert(clause.evaluate(binding) == EvaluationResult.TRUE)
    }

    @Test
    fun `AndClause evaluates correctly`() {
        val literals = Array(2) { Literal.create() }
        val clause = literals[0] + !literals[1]
        val binding = Binding()
        binding[literals[0]] = true
        binding[literals[1]] = false
        assert(clause.evaluate(binding) == EvaluationResult.TRUE)
    }

}

