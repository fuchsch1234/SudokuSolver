package de.fuchsch.satsolver

/**
 * Possible results for evaluating a term or formula.
 */
enum class EvaluationResult {
    /**
     * The boolean value of a [Cnf], [Cnf.Term], [Dnf] or [Dnf.Term] is true for a given binding.
     */
    TRUE,
    /**
     * The boolean value of a [Cnf], [Cnf.Term], [Dnf] or [Dnf.Term] is not completely defined for a given binding.
     */
    UNDEFINED,
    /**
     * The boolean value of a [Cnf], [Cnf.Term], [Dnf] or [Dnf.Term] is false for a given binding.
     */
    FALSE
}

/**
 * Join two [EvaluationResult]s using the or operator.
 *
 * @param rhs Right hand side evaluation result.
 * @return this or rhs.
 */
infix fun EvaluationResult.or(rhs: EvaluationResult): EvaluationResult = minOf(this, rhs)

/**
 * Join two [EvaluationResult]s using the and operator.
 *
 * @param rhs Right hand side evaluation result.
 * @return this and rhs.
 */
infix fun EvaluationResult.and(rhs: EvaluationResult): EvaluationResult = maxOf(this, rhs)

/**
 * Logical not of [EvaluationResult].
 *
 * @return Logic negation of this.
 */
operator fun EvaluationResult.not(): EvaluationResult = when (this) {
    EvaluationResult.TRUE -> EvaluationResult.FALSE
    EvaluationResult.FALSE -> EvaluationResult.TRUE
    else -> EvaluationResult.UNDEFINED
}

/**
 * Models the binding of [Variables][Variable] to boolean values.
 *
 * @property boundVariable The mutable binding from Variables to boolean values.
 */
data class Binding(
    val boundVariable: MutableMap<Variable, Boolean> = mutableMapOf()
) {

    /**
     * Queries the Binding if a variable is bound by it.
     *
     * @param variable The variable to test against this binding.
     * @return True if the variable is bound by this binding, otherwise false.
     */
    fun binds(variable: Variable): Boolean = boundVariable.containsKey(variable)

    /**
     * Evaluates a [Variable].
     *
     * @param variable The variable to evaluate.
     * @return A [EvaluationResult] representing the variables value in this binding.
     */
    fun evaluate(variable: Variable): EvaluationResult = when (boundVariable[variable]) {
        true -> EvaluationResult.TRUE
        false -> EvaluationResult.FALSE
        else -> EvaluationResult.UNDEFINED
    }

}
