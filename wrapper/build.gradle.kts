@file:OptIn(ExperimentalPathApi::class)

import kotlin.io.path.ExperimentalPathApi

plugins {
    kotlin("jvm")
    id("com.gradleup.shadow") version "9.6.1"
    id("net.typho.typho_publish")
}

group = "net.typho"
version = rootProject.version

repositories {
    mavenCentral()
    maven("https://typho.net/maven")
}

dependencies {
    implementation("net.typho:maven_util:1.5.1")
}

kotlin {
    jvmToolchain(21)
}

tasks.shadowJar {
    dependsOn(project(":").tasks.named("publishToMavenLocal"))

    archiveClassifier = ""
    configurations = listOf(project.configurations.getByName("runtimeClasspath"))

    dependsOn(project(":").tasks.jar)

    from(project(":").tasks.jar.get().archiveFile) {
        into("crucible")
    }

    manifest {
        attributes(
            "Main-Class" to "net.typho.crucible.wrapper.CrucibleWrapperInit"
        )
    }

    doLast {
        archiveFile.get().asFile.copyTo(rootProject.file("test/crucible.jar"), overwrite = true)
    }
}