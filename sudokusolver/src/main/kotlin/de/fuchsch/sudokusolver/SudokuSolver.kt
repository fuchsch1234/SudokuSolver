package de.fuchsch.sudokusolver

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.concurrent.Task
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.util.StringConverter
import tornadofx.*

class Model {
    private val grid = SimpleObjectProperty(Array(9) { IntArray(9) })
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

class MyViewModel: ItemViewModel<Model>() {
    private var task: Task<Unit?>? = null

    fun cellProperty(row: Int, column: Int) = bind { item?.cellProperty(row, column) }

    fun solve() {
        task = runAsync { item?.solve() } ui { item?.update() }
    }

    fun abort() {
        task?.cancel()
    }
}

class HelloWorld: View("Sudoku") {
    private val model: MyViewModel by inject()

    init {
        model.item = Model()
    }

    override val root = vbox {
        gridpane {
            style {
                padding = box(10.px)
            }
            repeat(9) { row ->
                row {
                    repeat(9) { column ->
                        textfield(model.cellProperty(row, column)) {
                            style {
                                border = Border(BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths(0.5)))
                                maxWidth = 30.px
                                alignment = Pos.CENTER
                            }
                            validator {
                                when {
                                    it == null -> null
                                    Regex("[0-9]?") matches it -> null
                                    else -> error("Must be between 0 and 9")
                                }
                            }
                        }
                    }
                }
            }
        }
        hbox {
            alignment = Pos.CENTER
            padding = Insets(10.0, 10.0, 10.0, 10.0)
            button("Solve") {
                enableWhen(model.valid.toBinding())
                action {
                    model.commit()
                    model.solve()
                }
            }
            button("Cancel") {
                action {
                    model.abort()
                }
            }
        }
    }
}

class SudokuSolver: App(HelloWorld::class)