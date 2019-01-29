package de.fuchsch.satsolver

/**
 * Possible results for evaluating a term or formula.
 */
enum class EvaluationResult {
    /**
     * The boolean value of a [Cnf] or [Cnf.Term] is true for a given binding.
     */
    TRUE,
    /**
     * The boolean value of a [Cnf] or [Cnf.Term] is not completely defined for a given binding.
     */
    UNDEFINED,
    /**
     * The boolean value of a [Cnf] or [Cnf.Term] is false for a given binding.
     */
    FALSE
}

/**
 * Model of a boolean formula in conjunctive normal form, that is a conjunction of disjunction terms.
 *
 * @property variables List of variables used in the formulas disjunction terms.
 * @property terms List of disjunction terms that build the formula.
 */
class Cnf (val variables: MutableList<Variable> = mutableListOf()) {

    val terms = mutableListOf<Term>()

    fun evaluate(binding: Binding): EvaluationResult =
        if (terms.all { it.evaluate(binding) == EvaluationResult.TRUE}) {
            EvaluationResult.TRUE
        } else if (terms.any { it.evaluate(binding) == EvaluationResult.FALSE}) {
            EvaluationResult.FALSE
        } else {
            EvaluationResult.UNDEFINED
        }

    /**
     * Adds a term to the formula.
     *
     * @param term New term to add.
     */
    operator fun plusAssign(term: Term) {
        terms.add(term)
    }

    /**
     * Adds a single variable term.
     *
     * @param variable New term contains this one variable.
     */
    operator fun plusAssign(variable: Variable) {
        terms.add(Term() + variable)
    }

    /**
     * Creates a new formula identical to the old one plus one new term.
     *
     * @param term Additional term for the new formula.
     * @return A new formula.
     */
    operator fun plus(term: Term): Cnf {
        val newKnf = Cnf()
        newKnf.terms.addAll(terms)
        newKnf.terms.add(term)
        return newKnf
    }

    /**
     * Creates a new formula identical to the old one plus one new term with one variable.
     *
     * @param term Additional variable for the new formula.
     * @return A new formula.
     */
    operator fun plus(variable: Variable): Cnf {
        val newKnf = Cnf()
        newKnf.terms.addAll(terms)
        newKnf += variable
        return newKnf
    }

    /**
     * Model for a single disjunction term used to build [CNF] formulas.
     *
     * A term evaluates to [EvaluationResult.FALSE] under a [Binding], iff all positive variables in the term
     * evaluate to false and all negated variables evaluate to true.
     * Iff at least one positive variable evaluates to true or at least one negated variable evaluates
     * to false, the term evaluates [EvaluationResult.TRUE].
     * Iff the term is neither true nor false it is [EvaluationResult.UNDEFINED]
     *
     * @property positiveVariables List of non negated variables in the term.
     * @property negativeVariables List of negated variables in the term.
     */
    data class Term (
        val positiveVariables: MutableList<Variable> = mutableListOf(),
        val negativeVariables: MutableList<Variable> = mutableListOf()
    ) {

        /**
         * Evaluates this term against a [Binding] to a boolean value or [EvaluationResult.UNDEFINED].
         *
         * @param binding The binding that assigns boolean values to the variables in this term.
         * @return The result of evaluating this term against the binding.
         */
        fun evaluate(binding: Binding): EvaluationResult =
            if (positiveVariables.any{ binding.boundVariable[it] == true }
                || negativeVariables.any{ binding.boundVariable[it] == false })
            {
                EvaluationResult.TRUE
            } else {
                if (positiveVariables.any{ !binding.boundVariable.containsKey(it) }
                    || negativeVariables.any{ !binding.boundVariable.containsKey(it)})
                {
                    EvaluationResult.UNDEFINED
                } else {
                    EvaluationResult.FALSE
                }
            }

        /**
         * Creates a new term with an additional non negated variable.
         *
         * @param v The new variable that is present in the new term.
         * @return A new term that contains all of this variables and the new one.
         */
        operator fun plus(v: Variable): Term {
            val newTerm = this.copy()
            newTerm.positiveVariables.add(v)
            return newTerm
        }

        /**
         * Creates a new term with an additional negated variable.
         *
         * @param v The new variable that is present in the new term.
         * @return A new term that contains all of this variables and the new one.
         */
        operator fun minus(v: Variable): Term {
            val newTerm = this.copy()
            newTerm.negativeVariables.add(v)
            return newTerm
        }

        /**
         * Adds a new non negated variable to this term.
         *
         * @param v The new variable.
         */
        operator fun plusAssign(v: Variable) {
            positiveVariables.add(v)
        }

        /**
         * Adds a new negated variable to this term.
         *
         * @param v The new variable
         */
        operator fun minusAssign(v: Variable) {
            negativeVariables.add(v)
        }
    }

}