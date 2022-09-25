package net.thatapex.shellbot.ssh

interface SSHClient : AutoCloseable {

    val connected: Boolean

    suspend fun connect()

    suspend fun openShell(): SSHShell

}
