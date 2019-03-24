package de.fuchsch.satsolver

enum class EvaluationResult {
    TRUE,
    UNDEFINED,
    FALSE,
}

operator fun EvaluationResult.not() = when(this) {
    EvaluationResult.TRUE -> EvaluationResult.FALSE
    EvaluationResult.FALSE -> EvaluationResult.TRUE
    EvaluationResult.UNDEFINED -> EvaluationResult.UNDEFINED
}

infix fun EvaluationResult.or(rhs: EvaluationResult) = minOf(this, rhs)

infix fun EvaluationResult.and(rhs: EvaluationResult) = maxOf(this, rhs)

/**
 * Models the binding of [Variables][Variable] to boolean values.
 *
 * @property boundVariable The mutable binding from Variables to boolean values.
 */
data class Binding(
    val boundVariable: MutableMap<Variable, Boolean> = mutableMapOf()
) {

    operator fun set(variable: Variable, value: Boolean) {
        boundVariable[variable] = value
    }

    operator fun set(literal: Literal, value: Boolean) {
        boundVariable[literal.variable] = value
    }

    operator fun get(variable: Variable) = boundVariable[variable]

    operator fun get(literal: Literal) = boundVariable[literal.variable]

    fun deepCopy() = Binding(boundVariable.toMutableMap())

    /**
     * Queries the Binding if a variable is bound by it.
     *
     * @param variable The variable to test against this binding.
     * @return True if the variable is bound by this binding, otherwise false.
     */
    fun binds(variable: Variable): Boolean = boundVariable.contains(variable)

    /**
     * Evaluates a [Variable].
     *
     * @param variable The variable to evaluate.
     * @return A [Result] representing the variables value in this binding.
     */
    fun evaluate(variable: Variable): EvaluationResult = when (boundVariable[variable]) {
        true -> EvaluationResult.TRUE
        false -> EvaluationResult.FALSE
        else -> EvaluationResult.UNDEFINED
    }

}
