plugins {
    kotlin("jvm")
    id("com.gradleup.shadow") version "9.6.1"
}

group = "net.typho"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.maven:maven-model:3.9.11")
    implementation("org.apache.maven.resolver:maven-resolver-api:2.0.21")
    implementation("org.apache.maven.resolver:maven-resolver-util:2.0.21")
    implementation("org.apache.maven.resolver:maven-resolver-impl:2.0.21")
    implementation("org.apache.maven.resolver:maven-resolver-connector-basic:2.0.21")
    implementation("org.apache.maven.resolver:maven-resolver-transport-file:2.0.21")
    implementation("org.apache.maven.resolver:maven-resolver-supplier-mvn3:2.0.21")
}

kotlin {
    jvmToolchain(21)
}

tasks.shadowJar {
    archiveClassifier = ""
    configurations = listOf(project.configurations.getByName("runtimeClasspath"))
    from(project(":").tasks.named("jar")) {
        rename { "crucible.jar" }
    }

    manifest {
        attributes(
            "Main-Class" to "net.typho.crucible.wrapper.CrucibleWrapper"
        )
    }

    doLast {
        archiveFile.get().asFile.copyTo(rootProject.file("test/crucible.jar"), overwrite = true)
    }
}