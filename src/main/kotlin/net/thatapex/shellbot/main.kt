package net.thatapex.shellbot

import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.thatapex.shellbot.discord.BotConfiguration
import net.thatapex.shellbot.discord.DiscordBot
import net.thatapex.shellbot.shell.SSHManager
import net.thatapex.shellbot.shell.ShellManager
import net.thatapex.shellbot.ssh.jsch.JSchClientFactory
import net.thatapex.shellbot.ssh.key.SSHKeyUtils
import net.thatapex.shellbot.vm.VirtualMachineId
import net.thatapex.shellbot.vm.gcloud.GCloudProject
import net.thatapex.shellbot.vm.gcloud.GCloudZone
import net.thatapex.shellbot.vm.gcloud.manager.GCloudVirtualMachineManager
import java.io.File
import java.io.FileInputStream

suspend fun main() {
    val logger = KotlinLogging.logger { }
    logger.info { "Application is starting..." }

    val botConfig = BotConfiguration(
        token = Environment.TOKEN,
        channel = Snowflake(Environment.CHANNEL_ID),
        managers = Environment.BOT_MANAGERS.map { Snowflake(it) }
    )

    val vmManager = GCloudVirtualMachineManager(
        project = GCloudProject("discord-shell-bot"),
        zone = GCloudZone("europe-west3-b"),
        globalTag = "shell-bot-instance"
    )

    val sshKeyFile = File("ssh_key.pem")
    val sshKey = if (sshKeyFile.exists()) {
        logger.info { "Loading and SSH key from a file" }

        withContext(Dispatchers.IO) {
            FileInputStream(sshKeyFile).use { SSHKeyUtils.load(it) }
        }
    } else {
        logger.info { "Generating an ephemeral SSH key" }
        SSHKeyUtils.generate()
    }


    val sshManager = SSHManager(
        publicKey = sshKey.public,
        privateKey = sshKey.private,
        sshClientFactory = JSchClientFactory(),
        sshUsername = "user"
    )

    val shellManager = ShellManager(
        vmManager = vmManager,
        vmId = VirtualMachineId("shell-bot"),
        sshManager = sshManager
    )

    val bot = DiscordBot(
        config = botConfig,
        shellManager = shellManager
    )

    bot.start()
}
