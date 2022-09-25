package net.thatapex.shellbot.ssh.jsch

import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.thatapex.shellbot.ssh.SSHClient
import net.thatapex.shellbot.ssh.SSHShell


class JSchSSHClient(private val session: Session) : SSHClient {

    override val connected: Boolean
        get() = session.isConnected

    override suspend fun connect() {
        withContext(Dispatchers.IO) {
            if (!connected)
                session.connect()
        }
    }

    override suspend fun openShell(): SSHShell {
        return withContext(Dispatchers.IO) {
            val channel = session.openChannel("shell") as ChannelShell
            channel.setPty(false)
            channel.connect()
            JSchShell(channel)
        }
    }

    override fun close() {
        if (connected) {
            session.disconnect()
        }
    }
}
