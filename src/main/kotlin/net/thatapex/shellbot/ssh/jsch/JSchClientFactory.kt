package net.thatapex.shellbot.ssh.jsch

import com.jcraft.jsch.Identity
import com.jcraft.jsch.JSch
import com.jcraft.jsch.KeyPair
import net.thatapex.shellbot.ssh.SSHClient
import net.thatapex.shellbot.ssh.SSHClientFactory
import org.bouncycastle.openssl.PEMWriter
import java.io.StringWriter
import java.security.PrivateKey

class JSchClientFactory : SSHClientFactory {
    override fun create(user: String, host: String, port: Int, sshKey: PrivateKey): SSHClient {
        val jsch = JSch()
        jsch.addIdentity(this.createIdentity(sshKey), null)

        val session = jsch.getSession(user, host, port)
        session.setConfig("StrictHostKeyChecking", "no")

        return JSchSSHClient(session)
    }

    private fun createIdentity(sshKey: PrivateKey): Identity {
        val stringWriter = StringWriter()
        PEMWriter(stringWriter).use {
            it.writeObject(sshKey)
        }

        val keyPair = KeyPair.load(null, stringWriter.toString().encodeToByteArray(), null)
        return JSchKeyPairIdentity(keyPair)
    }

}