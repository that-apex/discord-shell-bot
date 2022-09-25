package net.thatapex.shellbot.shell

import net.thatapex.shellbot.ssh.SSHClient
import net.thatapex.shellbot.ssh.SSHClientFactory
import net.thatapex.shellbot.ssh.SSHShell
import net.thatapex.shellbot.ssh.key.SSHKeyUtils
import net.thatapex.shellbot.vm.VirtualMachine
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPublicKey

class SSHManager(
    private val publicKey: PublicKey,
    private val privateKey: PrivateKey,
    private val sshClientFactory: SSHClientFactory,
    val sshUsername: String
) {
    private var client: SSHClient? = null
    private var shell: SSHShell? = null

    var virtualMachine: VirtualMachine? = null
        set(value) {
            closeShell()
            field = value
        }

    val openShell: SSHShell?
        get() = this.shell?.takeIf { it.open }

    suspend fun openShell() {
        closeShell()

        val virtualMachine = virtualMachine
        require(virtualMachine != null) { "Virtual machine not set" }

        val client = sshClientFactory.create(
            user = sshUsername,
            host = virtualMachine.publicIp,
            port = virtualMachine.sshPort,
            sshKey = this.privateKey
        )

        client.connect()
        this.client = client
        this.shell = client.openShell()
    }

    fun closeShell() {
        shell?.close()
        shell = null
        client?.close()
        client = null
    }

    fun encodedPublicKey(): String {
        return SSHKeyUtils.encodeAsOpenSSH(this.publicKey as RSAPublicKey)
    }
}