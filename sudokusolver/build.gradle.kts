plugins {
    id("application")
    kotlin("jvm")
}

application {
    mainClassName = "$group.sudokusolver.SudokuApp"
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":satsolver-library"))

    implementation("no.tornado:tornadofx:1.7.17")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.1.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.1.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
