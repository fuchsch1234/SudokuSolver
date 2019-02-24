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

}