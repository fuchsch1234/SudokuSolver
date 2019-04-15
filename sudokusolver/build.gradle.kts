plugins {
    id("application")
    kotlin("jvm")
}

application {
    mainClassName = "$group.sudokusolver.SudokuSolver"
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":satsolver-library"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.1.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.1.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
