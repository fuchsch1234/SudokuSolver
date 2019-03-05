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
 * Models the binding of [Literals][Literal] to boolean values.
 *
 * @property boundVariable The mutable binding from Variables to boolean values.
 */
data class Binding(
    val boundVariable: MutableMap<Literal, Boolean> = mutableMapOf()
) {

    operator fun set(literal: Literal, value: Boolean) {
        boundVariable[literal] = value
    }

    operator fun get(literal: Literal) = boundVariable[literal]

    fun deepCopy() = Binding(boundVariable.toMutableMap())

    /**
     * Queries the Binding if a variable is bound by it.
     *
     * @param literal The variable to test against this binding.
     * @return True if the variable is bound by this binding, otherwise false.
     */
    fun binds(literal: Literal): Boolean = boundVariable.containsKey(literal)

    /**
     * Evaluates a [Literal].
     *
     * @param literal The variable to evaluate.
     * @return A [Result] representing the variables value in this binding.
     */
    fun evaluate(literal: Literal): EvaluationResult = when (boundVariable[literal]) {
        true -> EvaluationResult.TRUE
        false -> EvaluationResult.FALSE
        else -> EvaluationResult.UNDEFINED
    }

}
