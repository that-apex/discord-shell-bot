package net.thatapex.shellbot.vm.gcloud

import net.thatapex.shellbot.vm.properties.VirtualMachineProperty

object GCloudVirtualMachineProperties {
    val MACHINE_TYPE = VirtualMachineProperty("MACHINE_TYPE", "n1-standard-1")
    val SOURCE_IMAGE_PROJECT = VirtualMachineProperty("SOURCE_IMAGE_PROJECT", "debian-cloud")
    val SOURCE_IMAGE_NAME = VirtualMachineProperty("SOURCE_IMAGE_NAME", "debian-11")
    val DISK_SIZE_GB = VirtualMachineProperty("DISK_SIZE_GB", 10L)
    val NETWORK_NAME = VirtualMachineProperty("NETWORK_NAME", "global/networks/shell-bot")
}
