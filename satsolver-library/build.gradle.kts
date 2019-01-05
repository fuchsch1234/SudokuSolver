plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    compile(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.1.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.1.0")
}
