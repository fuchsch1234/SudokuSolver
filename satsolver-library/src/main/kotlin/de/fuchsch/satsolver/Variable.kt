package de.fuchsch.satsolver

import java.util.*

class Variable private constructor (private val id: UUID) {

    operator fun plus(v: Variable): Knf.Term {
        val term = Knf.Term()
        term.positiveVariables.add(this)
        term.positiveVariables.add(v)
        return term
    }

    operator fun minus(v: Variable): Knf.Term {
        val term = Knf.Term()
        term.positiveVariables.add(this)
        term.negativeVariables.add(v)
        return term
    }

    operator fun unaryMinus(): Knf.Term {
        val term = Knf.Term()
        term.negativeVariables.add(this)
        return term
    }

    operator fun unaryPlus(): Knf.Term {
        val term = Knf.Term()
        term.positiveVariables.add(this)
        return term
    }

    companion object {

        fun create(): Variable = Variable(UUID.randomUUID())

    }

}