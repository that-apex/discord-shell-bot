package net.thatapex.shellbot.discord.commands

import dev.kord.core.behavior.interaction.response.respond
import net.thatapex.shellbot.discord.DiscordBot
import net.thatapex.shellbot.discord.Reactions

suspend fun DiscordBot.registerSSHCommands() {
    registerSlashCommand(
        name = "ssh",
        description = "Connect to SSH",
        unrestricted = false,
    ) {
        handler {
            val response = interaction.deferPublicResponse()

            DiscordBot.logger.info { "/ssh issued by ${interaction.user.id}" }

            if (shellManager.connectToSSH()) {
                response.respond { content = "Connected!" }
                    .message.addReaction(Reactions.DONE)
            } else {
                response.respond { content = "There is no VM to connect to!" }
                    .message.addReaction(Reactions.ERROR)
            }
        }
    }
}
