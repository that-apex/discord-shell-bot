package net.thatapex.shellbot.ssh

import java.security.PrivateKey

interface SSHClientFactory {

    fun create(
        user: String,
        host: String,
        port: Int = 22,
        sshKey: PrivateKey
    ): SSHClient

}
