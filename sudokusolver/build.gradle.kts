plugins {
    id("application")
    kotlin("jvm")
}

version = "1.0.0"

application {
    mainClassName = "$group.sudokusolver.SudokuApp"
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":satsolver-library"))
    implementation("org.slf4j:slf4j-log4j12:1.7.26")

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
