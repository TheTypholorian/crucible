package net.typho.crucible

import net.typho.crucible.deps.DependencyCoordinates
import net.typho.crucible.deps.Repository
import java.util.jar.Manifest

abstract class AbstractProjectConfig {
    val log: ILog
        get() = LOG
    val repositories by Crucible::repositories
    val dependencies by Crucible::dependencies
    var mainClass by Crucible::mainClass

    fun mavenCentral() = Repository.Maven(Repository.MAVEN_CENTRAL)

    fun typhoNet() = Repository.Maven(Repository.TYPHO_NET)

    fun kotlin(module: String, version: String = Config.KOTLIN_VERSION) = DependencyCoordinates("org.jetbrains.kotlin:kotlin-$module:$version")
}