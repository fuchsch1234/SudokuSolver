package de.fuchsch.satsolver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CnfUnitTest {

    @Test
    fun `A single term evaluates to its value`() {
        val variable = Variable.create()
        val knf = Cnf(mutableListOf(variable))
        knf += variable
        val trueBinding = Binding(mutableMapOf(variable to true))
        val falseBinding = Binding(mutableMapOf(variable to false))
        assertEquals(EvaluationResult.TRUE, knf.evaluate(trueBinding))
        assertEquals(EvaluationResult.UNDEFINED, knf.evaluate(Binding()))
        assertEquals(EvaluationResult.FALSE, knf.evaluate(falseBinding))
    }

    @Test
    fun `Evaluating multiple terms works`() {
        val variables = Array(5) { Variable.create() }
        val knf = Cnf(variables.toMutableList())
        knf += variables[0] - variables[1]
        knf += variables[2] + variables[3] + variables[4]
        val binding = Binding(
            mutableMapOf(
                variables[0] to false,
                variables[1] to false,
                variables[2] to true,
                variables[3] to false,
                variables[4] to false
            ))
        assertEquals(EvaluationResult.TRUE, knf.evaluate(binding))
    }

    @Test
    fun `Evaluating with variables in multiple terms works`() {
        val variables = Array(5) { Variable.create() }
        val knf = Cnf(variables.toMutableList())
        knf += variables[0] - variables[1]
        knf += variables[2]
        knf += variables[0] - variables[3]
        knf += variables[0] - variables[4]
        knf += variables[3] + variables[4]
        val trueBinding = Binding(
            mutableMapOf(
                variables[0] to true,
                variables[1] to false,
                variables[2] to true,
                variables[3] to false,
                variables[4] to true
            ))
        val falseBinding = Binding(
            mutableMapOf(
                variables[0] to false,
                variables[1] to false,
                variables[2] to false,
                variables[3] to false
            ))
        assertEquals(EvaluationResult.TRUE, knf.evaluate(trueBinding))
        assertEquals(EvaluationResult.FALSE, knf.evaluate(falseBinding))
    }
}