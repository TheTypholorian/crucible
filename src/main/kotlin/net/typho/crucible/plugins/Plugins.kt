package net.typho.crucible.plugins

@Target(AnnotationTarget.FILE)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class Plugins(
    val repositories: Array<String> = ["https://repo1.maven.org/maven2/", "https://typho.net/maven/"],
    val plugins: Array<String>
)
