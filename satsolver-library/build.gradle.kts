plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("org.jetbrains.dokka")
    idea
}

version = "0.2.0"

dependencies {
    val arrow_version = "0.9.0"

    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.0")

    implementation("org.slf4j:slf4j-api:1.7.25")

    implementation("io.arrow-kt:arrow-core-data:$arrow_version")
    implementation("io.arrow-kt:arrow-optics:$arrow_version")
    kapt("io.arrow-kt:arrow-optics:$arrow_version")
    kapt("io.arrow-kt:arrow-meta:$arrow_version")

    testImplementation("org.slf4j:slf4j-log4j12:1.7.26")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.1.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.1.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

idea {
    module {
        sourceDirs.addAll(files(
                "build/generated/source/kapt/main",
                "build/generated/source/kapt/debug",
                "build/generated/source/kapt/release",
                "build/generated/source/kaptKotlin/main",
                "build/generated/source/kaptKotlin/debug",
                "build/generated/source/kaptKotlin/release",
                "build/tmp/kapt/main/kotlinGenerated"))
        generatedSourceDirs.addAll(files(
                "build/generated/source/kapt/main",
                "build/generated/source/kapt/debug",
                "build/generated/source/kapt/release",
                "build/generated/source/kaptKotlin/main",
                "build/generated/source/kaptKotlin/debug",
                "build/generated/source/kaptKotlin/release",
                "build/tmp/kapt/main/kotlinGenerated"))
    }
}
