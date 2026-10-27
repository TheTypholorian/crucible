package net.typho.crucible.deps

import net.typho.crucible.ide.data.DependencyPathType
import net.typho.crucible.ide.data.IDEDependency
import net.typho.crucible.wrapper.CrucibleWrapper
import net.typho.crucible.wrapper.DependencyName
import net.typho.crucible.wrapper.MavenCache
import java.nio.file.Path
import kotlin.io.path.absolutePathString

class Repositories : ArrayList<String>() {
    companion object {
        val MAVEN_LOCAL = CrucibleWrapper.mavenLocal
        const val MAVEN_CENTRAL = "https://repo1.maven.org/maven2"
        const val TYPHO_NET = "https://typho.net/maven"

    }

    fun maven(url: String) = add(url)

    fun mavenLocal() = maven(MAVEN_LOCAL)

    fun mavenCentral() = maven(MAVEN_CENTRAL)

    fun typhoNet() = maven(TYPHO_NET)

    fun find(name: DependencyName): Path? {
        return MavenCache.resolveArtifact(name, this)
    }

    fun findIDE(name: DependencyName): IDEDependency? {
        val main = find(name) ?: return null
        val sources = find(name.copy(classifier = "sources"))

        return IDEDependency(name.coordinates, buildList {
            add(IDEDependency.Path(DependencyPathType.BINARY, main.absolutePathString()))

            if (sources != null) {
                add(IDEDependency.Path(DependencyPathType.SOURCE, sources.absolutePathString()))
            }
        })
    }
}