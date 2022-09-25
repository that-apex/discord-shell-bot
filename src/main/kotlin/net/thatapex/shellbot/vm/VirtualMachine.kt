package net.thatapex.shellbot.vm

data class VirtualMachineId(val value: String)

interface VirtualMachine {

    val id: VirtualMachineId

    val hostname: String

    val publicIp: String

    val sshPort: Int

}
