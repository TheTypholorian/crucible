import org.gradle.kotlin.dsl.kotlin

plugins {
    kotlin("jvm")
    id("net.typho.typho_publish")
}

group = "net.typho"
version = "1.9.1"

repositories {
    mavenCentral()
    maven("https://typho.net/maven")
}

dependencies {
    implementation("net.typho:data_util:2.3.10")
}

kotlin {
    jvmToolchain(21)
}