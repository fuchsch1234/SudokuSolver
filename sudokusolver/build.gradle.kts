plugins {
    id("application")
    kotlin("jvm")
}

application {
    mainClassName = "$group.sudokusolver.SudokuSolver"
}

dependencies {
    compile(project(":satsolver-library"))
    compile(kotlin("stdlib"))
}