package net.typho.crucible.deps

import org.eclipse.aether.artifact.Artifact
import java.nio.file.Path

interface ResolvedDependency {
    val name: DependencyName
    val path: Path

    class Maven(
        @JvmField
        val artifact: Artifact
    ) : ResolvedDependency {
        override val name: DependencyName
            get() = DependencyName(artifact)
        override val path: Path
            get() = artifact.path
    }
}