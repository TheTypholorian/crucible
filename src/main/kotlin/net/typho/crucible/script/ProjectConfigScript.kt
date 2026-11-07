package net.typho.crucible.script

object ProjectConfigScript : ProjectScriptType("Crucible Project Config", "crucible.kts") {
    init {
        defaultImports.addAll(listOf(
            "net.typho.crucible.*",
            "net.typho.crucible.Crucible.*",
            "net.typho.crucible.wrapper.CrucibleWrapper.*",
            "net.typho.crucible.deps.*",
            "net.typho.crucible.property.*",
            "net.typho.crucible.source.*",
            "net.typho.crucible.task.*",
            "net.typho.crucible.task.Task.*",
            "net.typho.crucible.ide.data.*"
        ))
    }
}