package de.fuchsch.sudokusolver

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.concurrent.Task
import javafx.geometry.Pos
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.util.StringConverter
import tornadofx.*

class Model(size: Int) {
    private val grid = SimpleObjectProperty(Array(size) { IntArray(size) })
    private var sudoku: Sudoku? = null

    fun cellProperty(row: Int, column: Int) = SimpleStringProperty(this, this.grid.value[row][column].toString()).apply {
        Bindings.bindBidirectional(this, grid, object: StringConverter<Array<IntArray>>() {
            override fun toString(obj: Array<IntArray>?): String {
                return obj?.get(row)?.get(column)?.toString() ?: "0"
            }

            override fun fromString(string: String?): Array<IntArray> {
                val newList = grid.value.copyOf()
                newList[row][column] = string?.toInt() ?: 0
                return newList
            }
        })
    }

    fun solve() {
        sudoku = Sudoku(grid.value)
        sudoku?.solve()
    }

    fun update() {
        val sudoku = sudoku
        if (sudoku != null) {
            grid.set(sudoku.grid)
        }
    }
}

open class SudokuViewModel: ItemViewModel<Model>() {

    private var task = SimpleObjectProperty<Task<Unit?>?>(null)

    val sizes = FXCollections.observableArrayList(*SudokuSize.values())
    val selectedSize = SimpleObjectProperty<SudokuSize>(SudokuSize.SUDOKU9x9)

    private val model9x9 = Model(9)
    private val model4x4 = Model(4)

    init {
        item = model9x9
        selectedSize.onChange { size -> size?.let { item = when(it) {
            SudokuSize.SUDOKU4x4 -> model4x4
            SudokuSize.SUDOKU9x9 -> model9x9
        }} }
    }

    val cancelable = Bindings.createBooleanBinding({ task.get() != null }, arrayOf(task))

    fun cellProperty(size: Int, row: Int, column: Int) = bind { when(size) {
            4 -> model4x4.cellProperty(row, column)
            9 -> model9x9.cellProperty(row, column)
            else -> throw IllegalArgumentException()
    } }

    fun solve() {
        task.set(runAsync { item?.solve() } ui { item?.update(); task.set(null) })
    }

    fun abort() {
        task.get()?.cancel()
        task.set(null)
    }

}

class SudokuView(size: Int): Fragment() {
    private val model: SudokuViewModel by inject()

    override val root = vbox {
        gridpane {
            style {
                padding = box(10.px)
                alignment = Pos.CENTER
            }
            repeat(size) { row ->
                row {
                    repeat(size) { column ->
                        textfield(model.cellProperty(size, row, column)) {
                            style {
                                border = Border(BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths(0.5)))
                                maxWidth = 30.px
                                alignment = Pos.CENTER
                            }
                            validator {
                                when {
                                    it == null -> null
                                    Regex("[0-$size]?") matches it -> null
                                    else -> error("Must be between 0 and $size")
                                }
                            }
                        }
                    }
                }
            }
        }
        hbox {
            style {
                alignment = Pos.CENTER
                padding = box(10.px)
            }
            button("Solve") {
                enableWhen(model.valid.toBinding())
                action {
                    model.commit()
                    model.solve()
                }
            }
            button("Cancel") {
                enableWhen(model.cancelable)
                action {
                    model.abort()
                }
            }
        }
    }
}

class SudokuSolver: View("Sudoku") {
    private val model: SudokuViewModel by inject()

    private val sudoku9x9 = SudokuView(9)
    private val sudoku4x4 = SudokuView(4)

    init {
        model.selectedSize.onChange { size ->
            size?.let {
                when(it) {
                    SudokuSize.SUDOKU9x9 -> sudoku4x4.replaceWith(sudoku9x9, sizeToScene = true)
                    SudokuSize.SUDOKU4x4 -> sudoku9x9.replaceWith(sudoku4x4, sizeToScene = true)
                }
            }
        }
    }

    override val root = vbox {
        style {
            padding = box(10.px)
            alignment = Pos.CENTER
        }
        combobox(model.selectedSize, model.sizes)
        add(sudoku9x9)
    }
}

class SudokuApp: App(SudokuSolver::class)