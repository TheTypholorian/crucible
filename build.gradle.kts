plugins {
    kotlin("jvm") version "2.4.0"
}

group = "net.typho"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.4.0")
}

kotlin {
    jvmToolchain(25)
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "net.typho.crucible.Crucible"
        )
    }
}