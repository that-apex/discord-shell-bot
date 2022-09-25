package net.thatapex.shellbot.discord

import dev.kord.core.Kord
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import mu.KotlinLogging
import net.thatapex.shellbot.discord.commands.registerSSHCommands
import net.thatapex.shellbot.discord.commands.registerVmCommands
import net.thatapex.shellbot.shell.ShellManager

class DiscordBot(
    val config: BotConfiguration,
    val shellManager: ShellManager
) {

    lateinit var kord: Kord
        private set

    lateinit var guild: Guild
        private set

    lateinit var channel: TextChannel
        private set

    suspend fun start() {
        kord = Kord(config.token)

        channel = kord.getChannel(config.channel) as? TextChannel
            ?: throw IllegalStateException("Channel ${config.channel} is not a text channel or it doesn't exist")

        guild = channel.getGuild()

        logger.info { "Bot starting in channel $channel and guild $guild" }
        registerCommands()

        logger.info { "Connecting..." }

        kord.on<ReadyEvent> {
            logger.info { "Connected!" }
            shellManager.shellReader(channel)
        }

        kord.on<MessageCreateEvent> {
            if (message.getChannelOrNull() != channel || message.author?.isBot != false) {
                return@on
            }

            logger.info { "${message.author?.id} executed ${message.content}" }

            shellManager.executeShellCommand(message.content)
        }

        kord.login {
            @OptIn(PrivilegedIntent::class)
            intents += Intent.MessageContent
        }
    }

    private suspend fun registerCommands() {
        registerVmCommands()
        registerSSHCommands()
    }

    companion object {
        val logger = KotlinLogging.logger { }
    }
}
