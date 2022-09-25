package net.thatapex.shellbot.discord.commands

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.rest.builder.interaction.subCommand
import net.thatapex.shellbot.discord.DiscordBot
import net.thatapex.shellbot.discord.Reactions

suspend fun DiscordBot.registerVmCommands() {
    registerSlashCommand(
        name = "vm",
        description = "Manage the VM",
        unrestricted = false,
    ) {
        subCommand("create", "Create a VM")
        subCommand("destroy", "Destroy a VM")
        subCommand("info", "Get information about the VM")

        handler {
            val response = interaction.deferPublicResponse()

            when((interaction.command as SubCommand).name) {
                "create" -> {
                    shellManager.createVirtualMachine()
                    response.respond { content = "Virtual machine created!" }
                        .message.addReaction(Reactions.DONE)
                }
                "destroy" -> {
                    shellManager.deleteVirtualMachine()
                    response.respond { content = "Virtual machine destroyed!" }
                        .message.addReaction(Reactions.DONE)
                }
                "info" -> {
                    val vm = shellManager.getVirtualMachine()

                    if (vm != null) {
                        response.respond { content = "VM exists: $vm" }
                            .message.addReaction(Reactions.DONE)
                    } else {
                        response.respond { content = "VM does not exist" }
                            .message.addReaction(Reactions.DONE)
                    }
                }
            }
        }
    }
}
