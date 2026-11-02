@file:OptIn(ExperimentalPathApi::class)

import java.util.Properties
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.deleteRecursively

plugins {
    kotlin("jvm")
    id("com.gradleup.shadow") version "9.6.1"
}

group = "net.typho"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://typho.net/maven")
}

dependencies {
    implementation("net.typho:maven_util:1.5.1")
    implementation("net.typho:misc_util:1.1.0")
    implementation("net.typho:data_util:2.4.0")
}

kotlin {
    jvmToolchain(21)
}

tasks.shadowJar {
    dependsOn(project(":").tasks.named("publishToMavenLocal"))

    archiveClassifier = ""
    configurations = listOf(project.configurations.getByName("runtimeClasspath"))

    manifest {
        attributes(
            "Main-Class" to "net.typho.crucible.wrapper.CrucibleWrapper"
        )
    }

    doLast {
        archiveFile.get().asFile.copyTo(rootProject.file("test/crucible.jar"), overwrite = true)

        val propertiesFile = rootProject.file("test/crucible.properties")
        val properties = Properties()
        propertiesFile.inputStream().use { properties.load(it) }
        properties["crucible.version"] = rootProject.version
        propertiesFile.outputStream().use { properties.store(it, null) }

        Path(System.getProperty("user.home"), ".crucible", "caches", "maven", "net", "typho", "crucible", rootProject.version.toString()).deleteRecursively()
    }
}