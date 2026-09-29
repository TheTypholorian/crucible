package net.typho.crucible

import net.typho.crucible.deps.DependencyCoordinates
import net.typho.crucible.deps.Repository
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.baseClass
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm

abstract class ProjectConfig {
    val log: ILog
        get() = LOG
    val repositories by Crucible::repositories
    val dependencies by Crucible::dependencies

    fun mavenCentral() = Repository.Maven(Repository.MAVEN_CENTRAL)

    fun typhoNet() = Repository.Maven(Repository.TYPHO_NET)

    fun kotlin(module: String, version: String = Config.KOTLIN_VERSION) = DependencyCoordinates("org.jetbrains.kotlin:kotlin-$module:$version")

    object ScriptConfig : ScriptCompilationConfiguration({
        baseClass(ProjectConfig::class)
        jvm {
            dependenciesFromCurrentContext(wholeClasspath = true)
        }
    }) {
        private fun readResolve(): Any = ScriptConfig
    }
}