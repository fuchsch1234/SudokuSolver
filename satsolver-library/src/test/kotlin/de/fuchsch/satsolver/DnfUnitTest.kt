package de.fuchsch.satsolver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DnfUnitTest {

    @Test
    fun `A single term evaluates to its value`() {
        val variable = Variable.create()
        val dnf = Dnf(mutableSetOf(variable))
        dnf += variable
        val trueBinding = Binding(mutableMapOf(variable to true))
        val falseBinding = Binding(mutableMapOf(variable to false))
        assertEquals(EvaluationResult.TRUE, dnf.evaluate(trueBinding))
        assertEquals(EvaluationResult.UNDEFINED, dnf.evaluate(Binding()))
        assertEquals(EvaluationResult.FALSE, dnf.evaluate(falseBinding))
    }

    @Test
    fun `Evaluating multiple terms works`() {
        val variables = Array(5) { Variable.create() }
        val dnf = Dnf(variables.toMutableSet())
        dnf += Dnf.Term() + variables[0] - variables[1]
        dnf += Dnf.Term() + variables[2] + variables[3] + variables[4]
        val binding = Binding(
            mutableMapOf(
                variables[0] to false,
                variables[1] to true,
                variables[2] to true,
                variables[3] to false,
                variables[4] to false
            ))
        assertEquals(EvaluationResult.FALSE, dnf.evaluate(binding))
    }

    @Test
    fun `Evaluating with variables in multiple terms works`() {
        val variables = Array(5) { Variable.create() }
        val dnf = Dnf(variables.toMutableSet())
        dnf += Dnf.Term() + variables[0] - variables[1]
        dnf += Dnf.Term() + variables[2]
        dnf += Dnf.Term() + variables[0] - variables[3]
        dnf += Dnf.Term() + variables[0] - variables[4]
        dnf += Dnf.Term() - variables[3] + variables[2]
        val trueBinding = Binding(
            mutableMapOf(
                variables[0] to true,
                variables[1] to false,
                variables[2] to true,
                variables[3] to false,
                variables[4] to false
            ))
        val falseBinding = Binding(
            mutableMapOf(
                variables[0] to false,
                variables[1] to false,
                variables[2] to false,
                variables[3] to true
            ))
        assertEquals(EvaluationResult.TRUE, dnf.evaluate(trueBinding))
        assertEquals(EvaluationResult.FALSE, dnf.evaluate(falseBinding))
    }
}