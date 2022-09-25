package net.thatapex.shellbot.vm.manager

import net.thatapex.shellbot.vm.VirtualMachine
import net.thatapex.shellbot.vm.VirtualMachineId
import net.thatapex.shellbot.vm.properties.VirtualMachineProperties

interface VirtualMachineManager {

    suspend fun createVirtualMachine(
        id: VirtualMachineId,
        properties: VirtualMachineProperties = VirtualMachineProperties.EMPTY
    ): VirtualMachine

    suspend fun findVirtualMachine(id: VirtualMachineId): VirtualMachine?

    suspend fun deleteVirtualMachine(machine: VirtualMachine)

}
