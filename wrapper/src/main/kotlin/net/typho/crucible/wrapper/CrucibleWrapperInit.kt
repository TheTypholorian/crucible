package net.typho.crucible.wrapper

object CrucibleWrapperInit {
    @JvmStatic
    fun main(args: Array<String>) {
        args.filter { it[0] == '-' }.forEach { property ->
            val index = property.indexOf('=')

            if (index == -1) {
                throw IllegalArgumentException("Property '$property' must have a '=' delimiter")
            }

            System.setProperty(property.substring(1, index), property.substring(index + 1))
        }
        CrucibleWrapper.start(args.filter { it[0] != '-' }.toTypedArray())
    }
}