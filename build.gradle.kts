import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings

plugins {
    kotlin("jvm") version "2.4.0"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.4.1"
}

group = "net.typho"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://typho.net/maven")
}

dependencies {
    implementation("net.typho:data_util:1.3.4")
    implementation("net.typho:misc_util:1.0.0")

    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.4.0")
    implementation("org.jetbrains.kotlin:kotlin-scripting-common:2.4.0")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:2.4.0")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host:2.4.0")

    implementation("org.apache.maven:maven-model:3.9.11")
    implementation("org.apache.maven.resolver:maven-resolver-api:2.0.21")
    implementation("org.apache.maven.resolver:maven-resolver-util:2.0.21")
    implementation("org.apache.maven.resolver:maven-resolver-impl:2.0.21")
    implementation("org.apache.maven.resolver:maven-resolver-connector-basic:2.0.21")
    implementation("org.apache.maven.resolver:maven-resolver-transport-file:2.0.21")
    implementation("org.apache.maven.resolver:maven-resolver-supplier-mvn3:2.0.21")
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

idea {
    project {
        settings {
            runConfigurations {
                create("Crucible", Application::class.java) {
                    mainClass = "net.typho.crucible.Crucible"
                    moduleName = "crucible.main"
                    jvmArgs = "-Dcrucible.project_root=${file("test").absolutePath}"
                }
            }
        }
    }
}