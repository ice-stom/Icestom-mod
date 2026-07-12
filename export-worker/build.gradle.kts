plugins {
    id("java")
    id("com.gradleup.shadow") version ("8.3.0")
}

group = "io.gitlab.icestom.icestom_mod"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.minestom:minestom:2026.01.08-1.21.11")
    implementation("dev.hollowcube:polar:1.15.0")
    implementation("it.unimi.dsi:fastutil:8.5.13")
}

tasks.shadowJar {
    archiveFileName = "icestom-export-worker.jar"

    manifest {
        attributes("Main-Class" to "ExportWorker")
    }
}