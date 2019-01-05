package de.fuchsch.satsolver

import org.junit.jupiter.api.Test

class KnfUnitTest {

    @Test
    fun `A single term evaluates to its value`() {
        val variable = Variable.create()
        val knf = Knf()
        knf += variable
        val trueBinding = Binding(mutableMapOf(variable to true))
        val falseBinding = Binding(mutableMapOf(variable to false))
        assert(knf.evaluate(trueBinding))
        assert(knf.evaluate(falseBinding))
    }
}