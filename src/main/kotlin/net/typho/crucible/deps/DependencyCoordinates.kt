package net.typho.crucible.deps

data class DependencyCoordinates(
    @JvmField
    val group: String,
    @JvmField
    val artifact: String,
    @JvmField
    val version: String,
    @JvmField
    val classifier: String = "",
    @JvmField
    val extension: String = "jar"
) {
    constructor(components: List<String>) : this(components[0], components[1], components[2])

    constructor(text: String) : this(text.split(':').also {
        if (it.size != 3) {
            throw IllegalArgumentException("Dependency coordinates '$text' must have exactly 3 ':' separator characters, got ${it.size}")
        }
    })
}
