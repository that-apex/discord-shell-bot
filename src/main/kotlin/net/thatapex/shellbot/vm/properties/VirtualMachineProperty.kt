package net.thatapex.shellbot.vm.properties

data class VirtualMachineProperty<T>(val id: String, val defaultValue: T? = null)
