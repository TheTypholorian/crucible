pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://typho.net/maven")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("net.typho.typho_publish") version "1.0.4" apply false
}
rootProject.name = "crucible"
include("ide_data")

project(":ide_data").name = "crucible.ide_data"