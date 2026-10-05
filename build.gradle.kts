import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings

plugins {
    kotlin("jvm") version "2.4.0"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.4.1"
    id("com.gradleup.shadow") version "9.6.1"
}

group = "net.typho"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://typho.net/maven")
}

dependencies {
    implementation("net.typho:crucible.ide_data:1.0.3")
    implementation("net.typho:data_util:1.3.5")
    implementation("net.typho:asm_util:1.3.6")
    implementation("net.typho:misc_util:1.0.1")

    implementation(kotlin("compiler-embeddable"))
    implementation(kotlin("scripting-common"))
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-jvm-host"))

    implementation("org.apache.maven:maven-model:3.9.11")
    implementation("org.apache.maven.resolver:maven-resolver-api:2.0.21")
    implementation("org.apache.maven.resolver:maven-resolver-util:2.0.21")
    implementation("org.apache.maven.resolver:maven-resolver-impl:2.0.21")
    implementation("org.apache.maven.resolver:maven-resolver-connector-basic:2.0.21")
    implementation("org.apache.maven.resolver:maven-resolver-transport-file:2.0.21")
    implementation("org.apache.maven.resolver:maven-resolver-supplier-mvn3:2.0.21")
}

kotlin {
    jvmToolchain(21)
}

tasks.shadowJar {
    archiveClassifier = ""

    manifest {
        attributes(
            "Main-Class" to "net.typho.crucible.Crucible"
        )
    }

    doLast {
        archiveFile.get().asFile.copyTo(File("test/crucible.jar"), overwrite = true)
    }
}

idea {
    project {
        settings {
            runConfigurations {
                create("Crucible", Application::class.java) {
                    mainClass = "net.typho.crucible.Crucible"
                    moduleName = "crucible.main"
                    jvmArgs = "-Dcrucible.project_root=${file("test").absolutePath}\n--sun-misc-unsafe-memory-access=allow"
                    programParameters = "refresh_ide print_classpath run"
                }
            }
        }
    }
}