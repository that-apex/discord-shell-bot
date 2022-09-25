package net.thatapex.shellbot.ssh.jsch

import com.jcraft.jsch.ChannelShell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.thatapex.shellbot.ssh.SSHShell
import java.io.InputStream
import java.io.PrintStream
import kotlin.math.min

class JSchShell(private val channel: ChannelShell) : SSHShell {

    private val stdin = PrintStream(channel.outputStream)
    private val stdout = channel.inputStream
    private val stderr = channel.extInputStream

    override val open: Boolean
        get() = this.channel.isConnected

    override suspend fun readData(): String {
        val stderr = read(stderr)

        if (stderr.isNotEmpty()) {
            return stderr
        }

        return read(stdout)
    }

    private suspend fun read(stream: InputStream): String {
        return withContext(Dispatchers.IO) {
            val available = stream.available()

            if (available < 0) {
                ""
            } else {
                val data = ByteArray(min(MAX_BUFFER_SIZE, available))
                stream.read(data)
                data.decodeToString()
            }
        }
    }

    override suspend fun sendCommand(command: String) {
        withContext(Dispatchers.IO) {
            stdin.print(command)
            stdin.print("\n")
            stdin.flush()
        }
    }

    override fun close() {
        stdin.close()
        stdout.close()
        stderr.close()

        if (!channel.isClosed)
            channel.disconnect()
    }

    companion object {
        private const val MAX_BUFFER_SIZE = 2048
    }
}