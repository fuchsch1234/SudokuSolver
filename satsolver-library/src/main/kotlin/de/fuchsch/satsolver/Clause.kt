package de.fuchsch.satsolver

import java.util.concurrent.atomic.AtomicLong

data class Variable private constructor (val id: Long) {

    companion object {

        private val nextId = AtomicLong(1)

        fun create() = Variable(nextId.getAndIncrement())

    }

}

sealed class Clause {

    open operator fun plus(clause: Clause): Clause = AndClause(mutableListOf(this, clause))

    open operator fun div(clause: Clause): Clause = OrClause(mutableListOf(this, clause))

    abstract fun evaluate(binding: Binding): EvaluationResult

    open val size: Int
        get() = throw NotImplementedError()

}

sealed class Literal (open val variable: Variable): Clause() {

    data class Positive(override val variable: Variable): Literal(variable) {

        override operator fun not(): Literal = Negation(variable)

        override fun evaluate(binding: Binding): EvaluationResult = binding.evaluate(variable)

    }

    data class Negation(override val variable: Variable): Literal(variable) {

        override operator fun not(): Literal = Positive(variable)

        override fun evaluate(binding: Binding): EvaluationResult = !binding.evaluate(variable)

    }

    abstract operator fun not(): Literal

    override val size: Int
        get() = 1

}

data class OrClause(val clauses: MutableList<Clause>): Clause() {

    constructor(clause: Clause): this(listOf(clause).toMutableList())

    override operator fun div(clause: Clause): Clause = OrClause((this.clauses + clause).toMutableList())

    operator fun divAssign(clause: Clause) {
        clauses.add(clause)
    }

    override fun evaluate(binding: Binding): EvaluationResult =
        clauses.fold(EvaluationResult.FALSE) { acc, clause ->
            acc or clause.evaluate(binding)
        }

    override val size: Int
        get() = clauses.size

}

data class AndClause(val clauses: MutableList<Clause>): Clause() {

    constructor(clause: Clause): this(listOf(clause).toMutableList())

    override operator fun plus(clause: Clause): Clause = AndClause((this.clauses + clause).toMutableList())

    operator fun plusAssign(clause: Clause) {
        clauses.add(clause)
    }

    override fun evaluate(binding: Binding): EvaluationResult =
        clauses.fold(EvaluationResult.TRUE) { acc, clause ->
            acc and clause.evaluate(binding)
        }

    override val size: Int
        get() = clauses.size

}
