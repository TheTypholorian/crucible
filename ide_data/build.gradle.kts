import org.gradle.kotlin.dsl.kotlin

plugins {
    kotlin("jvm")
    id("net.typho.typho_publish")
}

group = "net.typho"
version = "1.8.0"

repositories {
    mavenCentral()
    maven("https://typho.net/maven")
}

dependencies {
    implementation("net.typho:data_util:1.4.2")
}

kotlin {
    jvmToolchain(21)
}