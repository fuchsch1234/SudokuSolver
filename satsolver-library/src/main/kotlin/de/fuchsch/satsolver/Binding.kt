package de.fuchsch.satsolver

/**
 * Models the binding of [Variables][Variable] to boolean values.
 *
 * @property boundVariable The mutable binding from Variables to boolean values.
 */
data class Binding (
    val boundVariable: MutableMap<Variable, Boolean> = mutableMapOf()
) {

    /**
     * Queries the Binding if a variable is bound by it.
     *
     * @param variable The variable to test against this binding.
     * @return True if the variable is bound by this binding, otherwise false.
     */
    fun binds(variable: Variable): Boolean = boundVariable.containsKey(variable)

}