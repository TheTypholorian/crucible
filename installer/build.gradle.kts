plugins {
    id("java")
}

group = "net.typho"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
}

tasks.jar {
    archiveVersion = ""
    archiveBaseName = "crucible"

    manifest {
        attributes(
            "Main-Class" to "net.typho.crucible.installer.CrucibleInstaller"
        )
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}