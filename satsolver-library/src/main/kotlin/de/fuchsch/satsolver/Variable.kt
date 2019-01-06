package de.fuchsch.satsolver

import java.util.concurrent.atomic.AtomicLong

class Variable private constructor (private val id: Long) {

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

        private val id = AtomicLong(1)

        fun create(): Variable = Variable(id.getAndIncrement())

    }

}