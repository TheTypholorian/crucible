import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
}

dependencies {
    testImplementation(libs.junit)
    implementation("net.typho:crucible.ide_data:1.8.0") {
        isTransitive = false
    }
    implementation("net.typho:data_util:1.4.2") {
        isTransitive = false
    }

    intellijPlatform {
        intellijIdea("2026.1.4")
        testFramework(TestFrameworkType.Platform)
        bundledPlugin("org.jetbrains.kotlin")
    }
}
