package de.fuchsch.satsolver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KnfUnitTest {

    @Test
    fun `A single term evaluates to its value`() {
        val variable = Variable.create()
        val knf = Knf()
        knf += variable
        val trueBinding = Binding(mutableMapOf(variable to true))
        val falseBinding = Binding(mutableMapOf(variable to false))
        assertEquals(EvaluationResult.TRUE, knf.evaluate(trueBinding))
        assertEquals(EvaluationResult.UNDEFINED, knf.evaluate(Binding()))
        assertEquals(EvaluationResult.FALSE, knf.evaluate(falseBinding))
    }
}