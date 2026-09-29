plugins {
    kotlin("jvm")
}

group = "net.typho"

repositories {
    mavenCentral()
    maven("https://typho.net/maven")
}

dependencies {
    implementation(project(":"))
    implementation(kotlin("compiler-embeddable"))
    implementation(kotlin("scripting-common"))
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-jvm-host"))
}

kotlin {
    jvmToolchain(21)
}