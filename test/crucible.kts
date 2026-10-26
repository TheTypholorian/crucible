import net.typho.crucible.Crucible

repositories {
    mavenCentral()
    typhoNet()
}

val module = modules.register(name()) {
    sourceInputFolder.set(Crucible.sourceInputFolder())

    dependencies {
        kotlin("stdlib")
        add("net.typho:data_util:1.4.2")
        add("net.typho:asm_util:1.3.6")
    }

    java()
    resources()
    kotlin()

    compileTask()
    jarTask()
    runTask(mainClass = "Test")
}
