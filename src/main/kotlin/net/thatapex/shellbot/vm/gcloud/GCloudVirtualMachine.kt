package net.thatapex.shellbot.vm.gcloud

import net.thatapex.shellbot.vm.VirtualMachine
import net.thatapex.shellbot.vm.VirtualMachineId

data class GCloudVirtualMachine internal constructor(
    override val id: VirtualMachineId,
    override val hostname: String,
    override val publicIp: String,
    override val sshPort: Int,
    val project: GCloudProject,
    val zone: GCloudZone,
) : VirtualMachine {

}