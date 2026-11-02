import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.`maven-publish`
import org.jetbrains.gradle.ext.JarApplication
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings

plugins {
    kotlin("jvm") version "2.4.21"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.4.1"
    `maven-publish`
}

group = "net.typho"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://typho.net/maven")
}

dependencies {
    implementation("net.typho:crucible.ide_data:1.9.1")
    implementation("net.typho:data_util:2.4.0")
    implementation("net.typho:misc_util:1.1.0")

    implementation("org.vineflower:vineflower:1.12.0")
    implementation("org.semver4j:semver4j:6.0.0")

    implementation(kotlin("compiler-embeddable"))
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-jvm-host"))
    compileOnly(project("crucible.wrapper"))

    compileOnly("net.typho:maven_util:1.5.1")
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

fun crucibleTask(name: String, vararg params: String) = tasks.register<JavaExec>(name) {
    dependsOn(project("crucible.wrapper").tasks.named("shadowJar"))

    group = "crucible"
    classpath = files("test/crucible.jar")
    jvmArgs = listOf(
        "-Dcrucible.project_root=${file("test").absolutePath}"
    )
    args(*params)
}

crucibleTask("print_classpath", "print_classpath")
crucibleTask("run", "run")
crucibleTask("refresh_ide", "refresh_ide")