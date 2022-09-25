package net.thatapex.shellbot.shell

import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.TextChannel
import kotlinx.coroutines.delay
import net.thatapex.shellbot.vm.VirtualMachine
import net.thatapex.shellbot.vm.VirtualMachineId
import net.thatapex.shellbot.vm.manager.VirtualMachineManager
import net.thatapex.shellbot.vm.properties.VirtualMachineProperties
import kotlin.math.min

class ShellManager(
    private val vmManager: VirtualMachineManager,
    private val vmId: VirtualMachineId,
    private val sshManager: SSHManager
) {

    suspend fun createVirtualMachine() {
        vmManager.createVirtualMachine(
            id = vmId,
            properties = VirtualMachineProperties.of(
                VirtualMachineProperties.SSH_KEY to sshManager.encodedPublicKey(),
                VirtualMachineProperties.SSH_USER to sshManager.sshUsername
            )
        )
    }

    suspend fun getVirtualMachine(): VirtualMachine? {
        return vmManager.findVirtualMachine(vmId)
    }

    suspend fun deleteVirtualMachine(): Boolean {
        val vm = getVirtualMachine()
            ?: return false

        vmManager.deleteVirtualMachine(vm)
        return true
    }

    suspend fun connectToSSH(): Boolean {
        val vm = getVirtualMachine()
            ?: return false

        sshManager.virtualMachine = vm
        sshManager.openShell()

        return true
    }

    suspend fun shellReader(channel: TextChannel) {
        while (true) {
            val openShell = sshManager.openShell

            if (openShell == null) {
                delay(1000)
                continue
            }

            var remaining = openShell.readData()

            if (remaining.isEmpty()) {
                delay(1000)
                continue
            }

            while (remaining.isNotEmpty()) {
                val endIndex = min(MAX_MESSAGE_SIZE, remaining.length)

                val data = remaining.substring(0, endIndex)
                remaining = remaining.substring(endIndex, remaining.length)

                channel.createMessage {
                    content = "$PREFIX$data$SUFFIX`"
                }
            }
        }
    }

    suspend fun executeShellCommand(content: String): Boolean {
        val openShell = sshManager.openShell
            ?: return false

        openShell.sendCommand(content)
        return true
    }

    companion object {
        private const val PREFIX = "```ansi\n"
        private const val SUFFIX = "```"
        private const val MAX_MESSAGE_SIZE = 2000 - (PREFIX + SUFFIX).length
    }
}
