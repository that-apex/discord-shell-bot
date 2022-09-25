package net.thatapex.shellbot.ssh

interface SSHShell : AutoCloseable {

    val open: Boolean

    suspend fun readData(): String

    suspend fun sendCommand(command: String)

}
