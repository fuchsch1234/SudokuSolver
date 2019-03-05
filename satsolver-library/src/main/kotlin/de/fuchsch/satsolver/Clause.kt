package de.fuchsch.satsolver

import java.util.concurrent.atomic.AtomicLong


sealed class Clause {

    open operator fun plus(clause: Clause): Clause = AndClause(listOf(this, clause))

    open operator fun div(clause: Clause): Clause = OrClause(listOf(this, clause))

    abstract fun evaluate(binding: Binding): EvaluationResult

}

data class Literal private constructor (val id: Long): Clause() {

    companion object {

        private val nextId = AtomicLong(1)

        fun create() = Literal(nextId.getAndIncrement())

    }

    operator fun not(): Negation = Negation(this)

    override fun evaluate(binding: Binding): EvaluationResult = binding.evaluate(this)

}

data class Negation(val variable: Literal): Clause() {

    override fun evaluate(binding: Binding): EvaluationResult = !variable.evaluate(binding)

}

data class OrClause(val clauses: List<Clause>): Clause() {

    override operator fun div(clause: Clause): Clause = OrClause(this.clauses + clause)

    override fun evaluate(binding: Binding): EvaluationResult =
        clauses.fold(EvaluationResult.FALSE) { acc, clause ->
            acc or clause.evaluate(binding)
        }

}

data class AndClause(val clauses: List<Clause>): Clause() {

    override operator fun plus(clause: Clause): Clause = AndClause(this.clauses + clause)

    override fun evaluate(binding: Binding): EvaluationResult =
        clauses.fold(EvaluationResult.TRUE) { acc, clause ->
            acc and clause.evaluate(binding)
        }

}
