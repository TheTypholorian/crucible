package net.typho.crucible.script

import net.typho.crucible.property.PropertyContainer
import net.typho.crucible.wrapper.CrucibleClassLoader
import net.typho.crucible.wrapper.CrucibleWrapper
import kotlin.io.path.toPath
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.*
import kotlin.script.experimental.jvm.jvmTarget

abstract class ProjectScriptType(
    private val name: String,
    private val fileName: String,
    private val baseClass: KClass<*>? = null
) : PropertyContainer() {
    val defaultImports by listProperty<String>().fallback(listOf())
    var compilationConfig = ScriptCompilationConfiguration {
        displayName(name)
        fileExtension("kts")
        filePathPattern("^.*${fileName.replace(".", "\\.")}$")
        this@ProjectScriptType.baseClass?.let { baseClass(KotlinType(it)) }

        refineConfiguration {
            beforeCompiling { context ->
                context.compilationConfiguration
                    .with {
                        defaultImports.append(this@ProjectScriptType.defaultImports)

                        jvm {
                            jvmTarget("21")
                            dependencies.append(JvmDependency(CrucibleClassLoader.urLs.map { it.toURI().toPath().toFile() }))
                            CrucibleWrapper::class.java.protectionDomain.codeSource?.location?.let { dependencies.append(JvmDependency(it.toURI().toPath().toFile())) }
                        }

                        ide {
                            dependenciesSources.append(JvmDependency(CrucibleClassLoader.sources.map { it.toFile() }))
                        }
                    }
                    .asSuccess()
            }
        }
    }
        protected set
}