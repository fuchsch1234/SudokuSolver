package de.fuchsch.satsolver

/**
 * Possible results for evaluating a term or formula.
 */
enum class EvaluationResult {
    FALSE,
    TRUE,
    UNDEFINED
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

    operator fun plusAssign(term: Term) {
        terms.add(term)
    }

    operator fun plusAssign(variable: Variable) {
        terms.add(Term() + variable)
    }

    operator fun plus(term: Term): Cnf {
        val newKnf = Cnf()
        newKnf.terms.addAll(terms)
        newKnf.terms.add(term)
        return newKnf
    }

    operator fun plus(variable: Variable): Cnf {
        val newKnf = Cnf()
        newKnf.terms.addAll(terms)
        newKnf += variable
        return newKnf
    }

    /**
     * Model for a single disjunction term used to build [CNF] formulas.
     */
    data class Term (
        val positiveVariables: MutableList<Variable> = mutableListOf(),
        val negativeVariables: MutableList<Variable> = mutableListOf()
    ) {

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

        operator fun plus(v: Variable): Term {
            val newTerm = this.copy()
            newTerm.positiveVariables.add(v)
            return newTerm
        }

        operator fun minus(v: Variable): Term {
            val newTerm = this.copy()
            newTerm.negativeVariables.add(v)
            return newTerm
        }

        operator fun plusAssign(v: Variable) {
            positiveVariables.add(v)
        }

        operator fun minusAssign(v: Variable) {
            negativeVariables.add(v)
        }
    }

}