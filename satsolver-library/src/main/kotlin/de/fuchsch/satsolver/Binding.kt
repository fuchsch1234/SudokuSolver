package de.fuchsch.satsolver

data class Binding (
    val boundVariable: MutableMap<Variable, Boolean> = mutableMapOf()
) {

    fun binds(variable: Variable): Boolean = boundVariable.containsKey(variable)

}