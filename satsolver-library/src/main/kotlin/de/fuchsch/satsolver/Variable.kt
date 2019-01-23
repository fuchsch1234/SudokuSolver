package de.fuchsch.satsolver

import java.util.concurrent.atomic.AtomicLong

/**
 * Represents a variable for use in [Knf] boolean formulas.
 * A variable can be bound to either true or false by means of a [Binding] or it can be unassigned,
 * in which case its value is undefined.
 *
 * @property id A monotonically increasing Long, uniquely identifying every variable instance.
 * @constructor The constructor is private use [Variable.create] instead to create instances of variables.
 */
class Variable private constructor (private val id: Long) {

    /**
     * Adding two variables gives a term that models the disjunction of both variables.
     *
     * @param v The second variable for the disjunction.
     * @return A term modelling the disjunction.
     */
    operator fun plus(v: Variable): Knf.Term {
        val term = Knf.Term()
        term.positiveVariables.add(this)
        term.positiveVariables.add(v)
        return term
    }

    /**
     * Subtracting two variables gives a term that models the disjunction of both variables,
     * where the second variable is negated.
     *
     * @param v The second variable for the disjunction.
     * @return A term modelling the disjunction.
     */
    operator fun minus(v: Variable): Knf.Term {
        val term = Knf.Term()
        term.positiveVariables.add(this)
        term.negativeVariables.add(v)
        return term
    }

    /**
     * Convenience function to create one variable terms, in which the variable is negated.
     *
     * @return A single variable term, where the variable is negated.
     */
    operator fun unaryMinus(): Knf.Term {
        val term = Knf.Term()
        term.negativeVariables.add(this)
        return term
    }

    /**
     * Convenience function to create on variable terms.
     *
     * @return A single variable term.
     */
    operator fun unaryPlus(): Knf.Term {
        val term = Knf.Term()
        term.positiveVariables.add(this)
        return term
    }

    companion object {

        private val id = AtomicLong(1)

        /**
         * Factory function to create new Variables in a thread safe manner.
         */
        fun create(): Variable = Variable(id.getAndIncrement())

    }

}