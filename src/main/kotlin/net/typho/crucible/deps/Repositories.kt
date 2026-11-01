package net.typho.crucible.deps

import net.typho.crucible.ide.data.DependencyPathType
import net.typho.crucible.ide.data.IDEDependency
import net.typho.crucible.ide.data.IDEDependencyPath
import net.typho.crucible.wrapper.CrucibleWrapper
import net.typho.maven_util.ArtifactCoords
import net.typho.maven_util.ArtifactPointer
import net.typho.maven_util.LookupResult
import net.typho.maven_util.MavenUtil
import java.net.URI
import kotlin.io.path.absolutePathString

class Repositories : ArrayList<URI>() {
    fun maven(url: URI) = add(url)

    fun mavenLocal() = maven(MavenUtil.MAVEN_LOCAL.toUri())

    fun mavenCentral() = maven(MavenUtil.MAVEN_CENTRAL)

    fun typhoNet() = maven(MavenUtil.TYPHO_NET)

    fun findIDE(coords: ArtifactCoords): LookupResult<ArtifactPointer<IDEDependency>> {
        return MavenUtil.cachedLookup(coords, CrucibleWrapper.mavenCacheFolder, this).map { main ->
            val sources = MavenUtil.cachedLookup(
                coords.copy(classifier = "sources"),
                CrucibleWrapper.mavenCacheFolder,
                main.repository?.let { listOf(it) } ?: this
            ).value?.artifact

            ArtifactPointer(
                main.repository,
                IDEDependency(coords.toString(), buildList {
                    add(IDEDependencyPath(DependencyPathType.BINARY, main.artifact))

                    if (sources != null) {
                        add(IDEDependencyPath(DependencyPathType.SOURCE, sources))
                    }
                })
            )
        }
    }
}