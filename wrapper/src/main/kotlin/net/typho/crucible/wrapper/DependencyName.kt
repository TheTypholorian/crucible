package net.typho.crucible.wrapper

import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact

@JvmInline
value class DependencyName(
    /**
     * Should be in the format `<groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>`
     */
    @JvmField
    val coordinates: String
) {
    val components: List<String>
        get() = coordinates.split(':')
    val groupId: String
        get() = components[0]
    val artifactId: String
        get() = components[1]
    val extension: String?
        get() = components.let { if (it.size >= 4) it[2] else null }
    val classifier: String?
        get() = components.let { if (it.size >= 5) it[3] else null }
    val version: String
        get() = components.last()

    init {
        if (components.size < 3) {
            throw IllegalArgumentException("Illegal dependency name '$coordinates' (must be in the format <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>)")
        }
    }

    constructor(artifact: Artifact) : this(
        artifact.groupId,
        artifact.artifactId,
        artifact.version,
        if (artifact.extension.isEmpty() || artifact.extension == "jar") null else artifact.extension,
        artifact.classifier.ifEmpty { null }
    )

    constructor(
        groupId: String,
        artifactId: String,
        version: String,
        extension: String? = null,
        classifier: String? = null
    ) : this(buildString {
        append(groupId)
        append(':')
        append(artifactId)

        if (extension != null) {
            append(':')
            append(extension)

            if (classifier != null) {
                append(':')
                append(classifier)
            }
        } else if (classifier != null) {
            throw IllegalArgumentException("Dependency name cannot have a null extension and a non-null classifier")
        }

        append(':')
        append(version)
    })

    @JvmOverloads
    fun toArtifact(
        groupId: String = this.groupId,
        artifactId: String = this.artifactId,
        version: String = this.version,
        extension: String? = this.extension,
        classifier: String? = this.classifier
    ) = DefaultArtifact(
        groupId,
        artifactId,
        classifier,
        extension ?: "jar",
        version
    )

    @JvmOverloads
    fun matches(
        artifact: Artifact,
        checkGroup: Boolean = true,
        checkArtifact: Boolean = true,
        checkVersion: Boolean = true,
        checkExtension: Boolean = true,
        checkClassifier: Boolean = true
    ): Boolean {
        if (checkGroup && artifact.groupId != groupId) {
            return false
        }

        if (checkArtifact && artifact.artifactId != artifactId) {
            return false
        }

        if (checkVersion && artifact.version != version) {
            return false
        }

        if (checkExtension && artifact.extension != extension) {
            return false
        }

        if (checkClassifier && artifact.classifier != classifier) {
            return false
        }

        return true
    }

    fun toString(
        withGroup: Boolean = true,
        withArtifact: Boolean = true,
        withVersion: Boolean = true,
        withExtension: Boolean = true,
        withClassifier: Boolean = true
    ): String = buildList {
        if (withGroup) {
            add(groupId)
        }

        if (withArtifact) {
            add(artifactId)
        }

        if (withExtension) {
            extension?.let { add(it) }
        }

        if (withClassifier) {
            classifier?.let { add(it) }
        }

        if (withVersion) {
            add(version)
        }
    }.joinToString(separator = ":")

    override fun toString(): String {
        return coordinates
    }
}