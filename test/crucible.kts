import net.typho.crucible.Crucible

repositories {
    mavenCentral()
    typhoNet()
}

val module = modules.register(name()) {
    sourceInputFolder.set(Crucible.sourceInputFolder())

    dependencies {
        kotlin("stdlib")
        add("net.typho:data_util")
        add("net.typho:asm_util")
    }

    java()
    resources()
    kotlin()

    compileTask()
    jarTask()
    runTask(mainClass = "Test")
}
