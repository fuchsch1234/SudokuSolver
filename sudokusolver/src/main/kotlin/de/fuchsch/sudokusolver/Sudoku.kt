package de.fuchsch.sudokusolver

import java.security.InvalidParameterException

enum class SudokuSize(val size: Int) {
    SUDOKU4x4(4),
    SUDOKU9x9(9)
}

class Sudoku(private val size: SudokuSize = SudokuSize.SUDOKU9x9) {

    private val grid: Array<IntArray> = Array(size.size) { IntArray(size.size) }

    fun solve() {

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