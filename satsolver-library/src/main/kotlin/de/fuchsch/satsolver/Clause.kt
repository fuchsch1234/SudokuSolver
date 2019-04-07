package de.fuchsch.satsolver

import arrow.optics.optics
import java.util.concurrent.atomic.AtomicLong

data class Variable private constructor (val id: Long) {

    companion object {

        private val nextId = AtomicLong(1)

        fun create() = Variable(nextId.getAndIncrement())

    }

}

@optics data class CnfTerm(val literals: List<Literal>) {

    constructor(literal: Literal): this(listOf(literal).toMutableList())

    operator fun div(literal: Literal) = CnfTerm(literals + literal)

    operator fun div(term: CnfTerm) = CnfTerm(literals + term.literals)

    val size: Int
        get() = literals.size

    fun evaluate(binding: Binding): EvaluationResult =
        literals.fold(EvaluationResult.FALSE) { acc, literal ->
            acc or literal.evaluate(binding)
        }

    companion object {

        fun empty(): CnfTerm = CnfTerm(emptyList<Literal>().toMutableList())

    }
}

typealias Cnf = List<CnfTerm>

fun Cnf.evaluate(binding: Binding) = this.fold(EvaluationResult.TRUE) { acc, term -> acc and term.evaluate(binding)}

@optics sealed class Literal (open val variable: Variable) {

    data class Positive(override val variable: Variable): Literal(variable) {

        override operator fun not(): Literal = Negation(variable)

        override fun evaluate(binding: Binding): EvaluationResult = binding.evaluate(variable)

    }

    data class Negation(override val variable: Variable): Literal(variable) {

        override operator fun not(): Literal = Positive(variable)

        override fun evaluate(binding: Binding): EvaluationResult = !binding.evaluate(variable)

    }

    abstract fun evaluate(binding: Binding): EvaluationResult

    abstract operator fun not(): Literal

    operator fun div(literal: Literal) = CnfTerm(listOf(this, literal))

    companion object

}

fun exactlyOneOf(variables: Iterable<Variable>): List<CnfTerm> {
    val auxiliaryVariables = variables.map { Variable.create() }
    val result = emptyList<CnfTerm>().toMutableList()
    result += auxiliaryVariables.fold(CnfTerm.empty()) { term, variable ->
        term / Literal.Positive(variable)
    }
    variables.zip(auxiliaryVariables).forEach { (positiveVariable, auxiliary) ->
        variables.forEach { variable ->
            if (variable != positiveVariable) {
                result += Literal.Negation(auxiliary) / Literal.Negation(variable)
            } else {
                result += Literal.Negation(auxiliary) / Literal.Positive(variable)
            }
        }
    }
    return result
}