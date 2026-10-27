import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.`maven-publish`
import org.jetbrains.gradle.ext.Application
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings
import java.util.Properties

plugins {
    kotlin("jvm") version "2.4.21"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.4.1"
    `maven-publish`
}

group = "net.typho"
version = "0.1.${System.currentTimeMillis() / 1000}"

val propertiesFile = project.file("test/crucible.properties")
val properties = Properties()
propertiesFile.inputStream().use { properties.load(it) }
properties["crucible.version"] = project.version
propertiesFile.outputStream().use { properties.store(it, null) }

repositories {
    mavenCentral()
    maven("https://typho.net/maven")
}

dependencies {
    implementation("net.typho:crucible.ide_data:1.8.0")
    implementation("net.typho:data_util:1.4.2")
    implementation("net.typho:misc_util:1.1.0")

    implementation("org.vineflower:vineflower:1.12.0")
    implementation("org.semver4j:semver4j:6.0.0")

    implementation(kotlin("compiler-embeddable"))
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-jvm-host"))
    compileOnly(project("crucible.wrapper"))

    compileOnly("org.apache.maven:maven-model:3.9.11")
    compileOnly("org.apache.maven.resolver:maven-resolver-api:2.0.21")
    compileOnly("org.apache.maven.resolver:maven-resolver-util:2.0.21")
    compileOnly("org.apache.maven.resolver:maven-resolver-impl:2.0.21")
    compileOnly("org.apache.maven.resolver:maven-resolver-connector-basic:2.0.21")
    compileOnly("org.apache.maven.resolver:maven-resolver-transport-file:2.0.21")
    compileOnly("org.apache.maven.resolver:maven-resolver-supplier-mvn3:2.0.21")
}

kotlin {
    jvmToolchain(21)
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "net.typho.crucible.InvalidLaunchErrorer"
        )
    }
}

java {
    withSourcesJar()
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
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
                    programParameters = "print_classpath run"
                }
            }
        }
    }
}