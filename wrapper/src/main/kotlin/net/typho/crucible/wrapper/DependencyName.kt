package net.typho.crucible.wrapper

import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact

@JvmInline
value class DependencyName(
    /**
     * Must be in the format `<groupId>:<artifactId>[[:<classifier>]:<version>]`
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
    val classifier: String?
        get() = components.let { if (it.size >= 4) it[2] else null }
    val version: String?
        get() = components.let { if (it.size >= 3) it.last() else null }

    init {
        if (components.size !in 2..4) {
            throw IllegalArgumentException("Illegal dependency name '$coordinates' (must be in the format <groupId>:<artifactId>[[:<classifier>]:<version>])")
        }
    }

    constructor(artifact: Artifact) : this(
        artifact.groupId,
        artifact.artifactId,
        artifact.classifier.ifEmpty { null },
        artifact.version.ifEmpty { null }
    )

    constructor(
        groupId: String,
        artifactId: String,
        classifier: String? = null,
        version: String?
    ) : this(buildString {
        if (classifier != null && version == null) {
            throw IllegalArgumentException("Dependency name cannot have a null version and a non-null classifier")
        }

        append(groupId)
        append(':')
        append(artifactId)

        if (classifier != null) {
            append(':')
            append(classifier)
        }

        if (version != null) {
            append(':')
            append(version)
        }
    })

    @JvmOverloads
    fun toArtifact(
        groupId: String = this.groupId,
        artifactId: String = this.artifactId,
        classifier: String? = this.classifier,
        version: String? = this.version
    ) = DefaultArtifact(
        groupId,
        artifactId,
        classifier,
        "jar",
        version
    )

    @JvmOverloads
    fun copy(
        groupId: String = this.groupId,
        artifactId: String = this.artifactId,
        classifier: String? = this.classifier,
        version: String? = this.version
    ) = DependencyName(
        groupId = groupId,
        artifactId = artifactId,
        classifier = classifier,
        version = version
    )

    @JvmOverloads
    fun matches(
        artifact: Artifact,
        checkGroup: Boolean = true,
        checkArtifact: Boolean = true,
        checkClassifier: Boolean = true,
        checkVersion: Boolean = true
    ): Boolean {
        if (checkGroup && artifact.groupId != groupId) {
            return false
        }

        if (checkArtifact && artifact.artifactId != artifactId) {
            return false
        }

        if (checkClassifier && artifact.classifier != classifier) {
            return false
        }

        if (checkVersion && artifact.version != version) {
            return false
        }

        return true
    }

    fun toString(
        withGroup: Boolean = true,
        withArtifact: Boolean = true,
        withClassifier: Boolean = true,
        withVersion: Boolean = true
    ): String = buildList {
        if (withGroup) {
            add(groupId)
        }

        if (withArtifact) {
            add(artifactId)
        }

        if (withClassifier) {
            classifier?.let { add(it) }
        }

        if (withVersion) {
            version?.let { add(it) }
        }
    }.joinToString(separator = ":")

    override fun toString(): String {
        return coordinates
    }
}