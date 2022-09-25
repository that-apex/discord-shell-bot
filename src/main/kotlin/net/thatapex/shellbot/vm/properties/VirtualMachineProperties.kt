package net.thatapex.shellbot.vm.properties

data class VirtualMachineProperties(
    private val data: Map<VirtualMachineProperty<*>, Any>,
) {

    @Suppress("UNCHECKED_CAST")
    fun <T> getOrDefault(property: VirtualMachineProperty<T>, defaultValue: T): T {
        return getNullable(property)
            ?: defaultValue
    }

    operator fun <T> get(property: VirtualMachineProperty<T>): T {
        return getNullable(property)
            ?: throw IllegalArgumentException("No value for a required property $property")
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getNullable(property: VirtualMachineProperty<T>): T? {
        return data[property] as? T
            ?: property.defaultValue
    }

    fun has(property: VirtualMachineProperty<*>): Boolean =
        data.containsKey(property)

    companion object {
        val EMPTY = VirtualMachineProperties(emptyMap())

        fun of(vararg properties: Pair<VirtualMachineProperty<*>, Any>): VirtualMachineProperties =
            VirtualMachineProperties(properties.toMap())

        val SSH_KEY = VirtualMachineProperty<String>("SSH_KEY")
        val SSH_USER = VirtualMachineProperty<String>("SSH_USER", "user")
    }
}
