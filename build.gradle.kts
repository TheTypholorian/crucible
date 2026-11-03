plugins {
    kotlin("jvm") version "2.4.21"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.4.1"
    id("net.typho.typho_publish")
}

group = "net.typho"
version = "1.2.0"

repositories {
    mavenCentral()
    maven("https://typho.net/maven")
}

dependencies {
    implementation("net.typho:crucible.ide_data:2.1.1")
    implementation("net.typho:maven_util:1.5.1")
    implementation("net.typho:misc_util:1.1.0")
    implementation("net.typho:data_util:2.4.0")

    implementation("org.vineflower:vineflower:1.12.0")
    implementation("org.semver4j:semver4j:6.0.0")

    implementation(kotlin("compiler-embeddable"))
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-jvm-host"))
    implementation(project("crucible.wrapper"))
}

kotlin {
    jvmToolchain(21)
}

tasks.jar {
    archiveFileName.set("crucible.jar")
}

fun crucibleTask(name: String, vararg params: String) = tasks.register<JavaExec>(name) {
    dependsOn(project("crucible.wrapper").tasks.named("shadowJar"))

    group = "crucible"
    classpath = files("test/crucible.jar")
    args("-crucible.project_root=${file("test").absolutePath}")
    args(*params)
}

crucibleTask("print_classpath", "print_classpath")
crucibleTask("run", "run")
crucibleTask("refresh_ide", "refresh_ide")