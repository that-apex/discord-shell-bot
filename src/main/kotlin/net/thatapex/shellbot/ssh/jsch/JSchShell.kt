package net.thatapex.shellbot.ssh.jsch

import com.jcraft.jsch.ChannelShell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.thatapex.shellbot.ssh.SSHShell
import java.io.PrintStream
import kotlin.math.min

class JSchShell(private val channel: ChannelShell) : SSHShell {

    private val output = PrintStream(channel.outputStream)
    private val input = channel.inputStream

    override val open: Boolean
        get() = this.channel.isConnected

    override suspend fun readData(): String {
        return withContext(Dispatchers.IO) {
            val available = input.available()

            if (available < 0) {
                return@withContext ""
            } else {
                val data = ByteArray(min(MAX_BUFFER_SIZE, available))
                input.read(data)
                data.decodeToString()
            }
        }
    }

    override suspend fun sendCommand(command: String) {
        withContext(Dispatchers.IO) {
            output.print(command)
            output.println("\n")
            output.flush()
        }
    }

    override fun close() {
        output.close()

        if (!channel.isClosed)
            channel.disconnect()
    }

    companion object {
        private const val MAX_BUFFER_SIZE = 1024
    }
}