package de.fuchsch.sudokusolver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.security.InvalidParameterException

class SudokuTest {

    @Test
    fun `toString creates correct sudoku`() {
        val sudokuStr = "1 0 0 4\n" +
                "0 4 3 1\n" +
                "0 0 0 2\n" +
                "4 2 0 0"
        val sudoku = Sudoku.fromString(sudokuStr)
        assertEquals(sudokuStr, sudoku.toString())
    }

    @Test
    fun `fromString throws on invalid size`() {
        val sudokuStr = "0 0 0 0 0\n"
        assertThrows(InvalidParameterException::class.java) { Sudoku.fromString(sudokuStr) }
    }

    @Test
    fun `Can solve a 4x4 Sudoku`() {
        val sudokuString = "0 3 4 0\n" +
                "4 0 0 2\n" +
                "1 0 0 3\n" +
                "0 2 1 0"
        val sudokuSolution = "2 3 4 1\n" +
                "4 1 3 2\n" +
                "1 4 2 3\n" +
                "3 2 1 4"
        val sudoku = Sudoku.fromString(sudokuString)
        sudoku.solve()
        assertEquals(sudokuSolution, sudoku.toString())
    }

    @Test
    fun `Can solve a 9x9 Sudoku`() {
        val sudokuString =
            "0 4 0 3 0 1 2 8 0\n" +
            "9 3 2 0 0 6 0 0 1\n" +
            "8 1 7 0 0 5 0 4 0\n" +
            "0 0 0 0 3 2 0 5 4\n" +
            "0 0 4 0 9 0 6 2 0\n" +
            "2 6 3 0 0 0 0 1 8\n" +
            "4 9 8 2 6 7 0 0 0\n" +
            "0 2 1 0 0 0 0 0 7\n" +
            "0 0 0 4 0 3 8 0 2"
        val sudokuSolution =
            "5 4 6 3 7 1 2 8 9\n" +
            "9 3 2 8 4 6 5 7 1\n" +
            "8 1 7 9 2 5 3 4 6\n" +
            "1 8 9 6 3 2 7 5 4\n" +
            "7 5 4 1 9 8 6 2 3\n" +
            "2 6 3 7 5 4 9 1 8\n" +
            "4 9 8 2 6 7 1 3 5\n" +
            "3 2 1 5 8 9 4 6 7\n" +
            "6 7 5 4 1 3 8 9 2"
        val sudoku = Sudoku.fromString(sudokuString)
        sudoku.solve()
        assertEquals(sudokuSolution, sudoku.toString())
    }

}