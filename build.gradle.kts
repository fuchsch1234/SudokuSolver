plugins {
    id("base")
    kotlin("jvm").version("1.3.20").apply(false)
    kotlin("kapt").version("1.3.20").apply(false)
    id("org.jetbrains.dokka").version("0.9.17").apply(false)
}

allprojects {
    group = "de.fuchsch"
    version = "0.1.0-SNAPSHOT"

    repositories {
        jcenter()
        mavenCentral()
    }
}

dependencies {
    subprojects.forEach {
        archives(it)
    }
}
