package de.fuchsch.satsolver

data class Binding (
    val boundVariable: MutableMap<Variable, Boolean> = mutableMapOf()
)