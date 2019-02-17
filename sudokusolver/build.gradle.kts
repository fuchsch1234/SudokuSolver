plugins {
    id("application")
    kotlin("jvm")
}

application {
    mainClassName = "$group.SudokuSolver.SudokuSolver"
}

dependencies {
    compile(project(":satsolver-library"))
    compile(kotlin("stdlib"))
}