package net.typho.crucible.wrapper

import org.eclipse.aether.artifact.Artifact

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

    constructor(artifact: Artifact) : this(artifact.groupId, artifact.artifactId, artifact.version, artifact.extension, artifact.classifier)

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
        append(':')

        if (extension != null) {
            append(extension)
            append(':')

            if (classifier != null) {
                append(classifier)
                append(':')
            }
        } else if (classifier != null) {
            throw IllegalArgumentException("Dependency name cannot have a null extension and a non-null classifier")
        }

        append(version)
    })

    override fun toString(): String {
        return coordinates
    }
}