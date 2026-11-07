package net.typho.crucible.wrapper

object CrucibleWrapperInit {
    @JvmStatic
    fun main(args: Array<String>) {
        args.filter { it[0] == '-' }.associateTo(PropertyManager.properties) { property ->
            val index = property.indexOf('=')

            if (index == -1) {
                throw IllegalArgumentException("Property '$property' must have a '=' delimiter")
            }

            property.substring(1, index) to property.substring(index + 1)
        }

        CrucibleWrapper.start(args.filter { it[0] != '-' }.toTypedArray())
    }
}