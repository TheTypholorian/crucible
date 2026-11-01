import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
}

dependencies {
    testImplementation(libs.junit)
    implementation("net.typho:crucible.ide_data:1.9.1") {
        isTransitive = false
    }
    implementation("net.typho:data_util:2.3.10") {
        isTransitive = false
    }

    intellijPlatform {
        intellijIdea("2026.1.4")
        testFramework(TestFrameworkType.Platform)
        bundledPlugin("org.jetbrains.kotlin")
    }
}
