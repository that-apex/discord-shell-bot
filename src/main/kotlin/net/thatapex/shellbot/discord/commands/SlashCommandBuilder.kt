package net.thatapex.shellbot.discord.commands

import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.ChatInputCreateBuilder
import mu.KotlinLogging
import net.thatapex.shellbot.discord.DiscordBot

private val logger = KotlinLogging.logger { }

class SlashCommandBuilder(
    private val delegate: ChatInputCreateBuilder
) : ChatInputCreateBuilder by delegate {

    var handler: (suspend GuildChatInputCommandInteractionCreateEvent.() -> Unit)? = null

    fun handler(handler: (suspend GuildChatInputCommandInteractionCreateEvent.() -> Unit)) {
        this.handler = handler
    }
}

suspend fun DiscordBot.registerSlashCommand(
    name: String,
    description: String,
    unrestricted: Boolean = false,
    builder: SlashCommandBuilder.() -> Unit
) {
    var slashCommandBuilder: SlashCommandBuilder?

    val command = kord.createGuildChatInputCommand(guild.id, name, description) {
        slashCommandBuilder = SlashCommandBuilder(this)
        slashCommandBuilder!!.builder()
    }

    kord.on<GuildChatInputCommandInteractionCreateEvent> {
        if (command.id != interaction.command.rootId) {
            return@on
        }

        if (!unrestricted && !config.managers.contains(interaction.user.id)) {
            interaction.deferEphemeralResponse()
                .respond { content = "You do not have permission to use this command." }

            return@on
        }

        runCatching {
            slashCommandBuilder!!.handler?.invoke(this@on)
        }.onFailure {
            logger.error(it) { "Error while handling a slash command" }

            interaction.channel.createMessage(content = "An error occurred while handling your command.")
        }
    }
}
