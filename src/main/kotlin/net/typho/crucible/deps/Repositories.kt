package net.typho.crucible.deps

import net.typho.crucible.ide_data.IDELibraryData
import net.typho.crucible.ide_data.IDELibraryPathType
import net.typho.crucible.wrapper.CrucibleWrapper
import net.typho.crucible.wrapper.log.CrucibleDownloadListener
import net.typho.maven_util.ArtifactCoords
import net.typho.maven_util.ArtifactPointer
import net.typho.maven_util.LookupResult
import net.typho.maven_util.MavenUtil
import java.net.URI

class Repositories : ArrayList<URI>() {
    fun maven(url: URI) = add(url)

    fun mavenLocal() = maven(MavenUtil.MAVEN_LOCAL.toUri())

    fun mavenCentral() = maven(MavenUtil.MAVEN_CENTRAL)

    fun typhoNet() = maven(MavenUtil.TYPHO_NET)

    fun findIDE(coords: ArtifactCoords): LookupResult<ArtifactPointer<IDELibraryData>> {
        return MavenUtil.cachedLookup(
            coords,
            CrucibleWrapper.mavenCacheFolder,
            this,
            CrucibleDownloadListener
        ).map { main ->
            val sources = MavenUtil.cachedLookup(
                coords.copy(classifier = "sources"),
                CrucibleWrapper.mavenCacheFolder,
                main.repository?.let { listOf(it) } ?: this,
                CrucibleDownloadListener
            ).value?.artifact

            ArtifactPointer(
                main.repository,
                IDELibraryData(
                    externalName = coords.toString(),
                    paths = mapOf(
                        IDELibraryPathType.BINARY to listOf(main.artifact),
                        IDELibraryPathType.SOURCE to (sources?.let { listOf(it) } ?: listOf())
                    ),
                    groupId = coords.groupId,
                    artifactId = coords.artifactId,
                    version = coords.version
                )
            )
        }
    }
}