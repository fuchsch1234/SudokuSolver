package de.fuchsch.sudokusolver

import de.fuchsch.satsolver.*
import java.security.InvalidParameterException
import kotlin.math.sqrt

enum class SudokuSize(val size: Int) {
    SUDOKU4x4(4),
    SUDOKU9x9(9)
}

class Sudoku(private val size: SudokuSize = SudokuSize.SUDOKU9x9) {

    private val grid: Array<IntArray> = Array(size.size) { IntArray(size.size) }

    fun solve() {
        val variables = Array(size.size) { Array(size.size) { Array(size.size) { Variable.create() }}}
        val cnf = emptyList<CnfTerm>().toMutableList()
        // Only one number in each field
        for (x in 0 until size.size) {
            for (y in 0 until size.size) {
                cnf += exactlyOneOf(variables[y][x].toList())
            }
        }
        // Every line contains every number only once
        for (y in 0 until size.size) {
            for (n in 0 until size.size) {
                val line = mutableListOf<Variable>()
                for (x in 0 until size.size) {
                    line.add(variables[y][x][n])
                }
                cnf += exactlyOneOf(line)
            }
        }
        // Every column contains every number only once
        for (x in 0 until size.size) {
            for (n in 0 until size.size) {
                val line = mutableListOf<Variable>()
                for (y in 0 until size.size) {
                    line.add(variables[y][x][n])
                }
                cnf += exactlyOneOf(line)
            }
        }
        // Every quadrant contains every number only once
        val qsize = sqrt(size.size.toDouble()).toInt()
        for (x in 0 until qsize) {
            for (y in 0 until qsize) {
                for (n in 0 until size.size) {
                    val quadrant = mutableListOf<Variable>()
                    for (i in 0 until qsize) {
                        for (j in 0 until qsize) {
                            quadrant += variables[qsize * y + j][qsize * x + i][n]
                        }
                    }
                    cnf += exactlyOneOf(quadrant)
                }
            }
        }

        // Solve the Sudoku
        val binding = Binding()
        for (x in 0 until size.size) {
            for (y in 0 until size.size) {
                for (n in 0 until size.size) {
                    if (grid[y][x] == n + 1) cnf += CnfTerm(Literal.Positive(variables[y][x][n]))
                }
            }
        }
        val solution = de.fuchsch.satsolver.solve(cnf, binding)

        // Apply solution
        for (x in 0 until size.size) {
            for (y in 0 until size.size) {
                for (n in 0 until size.size) {
                    if (solution.evaluate(variables[y][x][n]) == EvaluationResult.TRUE) grid[y][x] = n + 1
                }
            }
        }
    }

    override fun toString(): String =
        grid.joinToString(separator = "\n") { row ->
            row.joinToString(separator = " ", transform = Int::toString)
        }

    companion object {

        fun fromString(str: String): Sudoku {
            val lines = str.split("\n")
            val size = SudokuSize.values().find { it.size == lines.size }
                ?: throw InvalidParameterException("Only sizes 4x4 and 9x9 are supported")
            val sudoku = Sudoku(size)
            lines.withIndex().map { (y, line) ->
                val elements = line.split(" ")
                if (elements.size != lines.size) {
                    throw InvalidParameterException("Lines must be same size as number of lines")
                }
                elements.withIndex().map { (x, element) ->
                    var element = element.toInt()
                    if (element < 0 || element > size.size) {
                        element = 0
                    }
                    sudoku.grid[y][x] = element
                }
            }
            return sudoku
        }

    }

}