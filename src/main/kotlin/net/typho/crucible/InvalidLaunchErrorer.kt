package net.typho.crucible

object InvalidLaunchErrorer {
    @JvmStatic
    fun main(args: Array<String>) {
        throw IllegalStateException("You launched crucible incorrectly, you need to run the crucible wrapper jar (which jar-in-jar's the main crucible jar)")
    }
}