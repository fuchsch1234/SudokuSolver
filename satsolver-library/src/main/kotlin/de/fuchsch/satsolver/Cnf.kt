package de.fuchsch.satsolver

/**
 * Model of a boolean formula in conjunctive normal form, that is a conjunction of disjunction terms.
 *
 * @property variables List of variables used in the formulas disjunction terms.
 * @property terms List of disjunction terms that build the formula.
 */
class Cnf (val variables: MutableSet<Variable> = mutableSetOf()) {

    val terms = mutableListOf<Term>()

    /**
     * Evaluates this formula against a binding that assigns boolean values to variables.
     *
     * The formula is evaluated by evaluating all its terms and combining their [EvaluationResult]s
     *
     * @param binding The [Binding] used to assign values to variables.
     * @return An [EvaluationResult] that represents this formulas evaluation against the binding.
     */
    fun evaluate(binding: Binding): EvaluationResult =
        terms.map { it.evaluate(binding) }.fold(EvaluationResult.TRUE, EvaluationResult::and)

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
     * @param variable Additional variable for the new formula.
     * @return A new formula.
     */
    operator fun plus(variable: Variable): Cnf {
        val newKnf = Cnf()
        newKnf.terms.addAll(terms)
        newKnf += variable
        return newKnf
    }

    /**
     * Model for a single disjunction term used to build [Cnf] formulas.
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
        val positiveVariables: MutableSet<Variable> = mutableSetOf(),
        val negativeVariables: MutableSet<Variable> = mutableSetOf()
    ) {

        /**
         * Evaluates this term against a [Binding] to a boolean value or [EvaluationResult.UNDEFINED].
         *
         * @param binding The binding that assigns boolean values to the variables in this term.
         * @return The result of evaluating this term against the binding.
         */
        fun evaluate(binding: Binding): EvaluationResult =
                positiveVariables.map { binding.evaluate(it) }.fold(EvaluationResult.FALSE, EvaluationResult::or).or(
                negativeVariables.map { binding.evaluate(it) }.fold(EvaluationResult.FALSE) { acc, v -> acc.or(!v) })

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