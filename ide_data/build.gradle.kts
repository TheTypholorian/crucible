plugins {
    kotlin("jvm")
    id("net.typho.typho_publish")
}

group = "net.typho"
version = "1.2.0"

repositories {
    mavenCentral()
    maven("https://typho.net/maven")
}

dependencies {
    implementation("net.typho:data_util:1.3.5")
}

kotlin {
    jvmToolchain(21)
}