package de.fuchsch.satsolver

class Knf {

    val terms = mutableListOf<Term>()

    fun evaluate(binding: Binding): Boolean = false

    operator fun plusAssign(term: Term) {
        terms.add(term)
    }

    operator fun plusAssign(variable: Variable) {
        terms.add(Term() + variable)
    }

    operator fun plus(term: Term): Knf {
        val newKnf = Knf()
        newKnf.terms.addAll(terms)
        newKnf.terms.add(term)
        return newKnf
    }

    operator fun plus(variable: Variable): Knf {
        val newKnf = Knf()
        newKnf.terms.addAll(terms)
        newKnf += variable
        return newKnf
    }

    data class Term (
        val positiveVariables: MutableList<Variable> = mutableListOf(),
        val negativeVariables: MutableList<Variable> = mutableListOf()
    ) {

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