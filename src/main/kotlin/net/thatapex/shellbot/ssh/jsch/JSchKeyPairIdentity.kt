package net.thatapex.shellbot.ssh.jsch

import com.jcraft.jsch.Identity
import com.jcraft.jsch.KeyPair

class JSchKeyPairIdentity(private val kpair: KeyPair) : Identity {
    override fun setPassphrase(passphrase: ByteArray?): Boolean = false

    override fun getPublicKeyBlob(): ByteArray = kpair.publicKeyBlob

    override fun getSignature(data: ByteArray?): ByteArray = kpair.getSignature(data)

    @Deprecated("Deprecated in JSch", replaceWith = ReplaceWith("setPassphrase"))
    override fun decrypt(): Boolean = throw RuntimeException("not implemented")

    override fun getAlgName(): String = "ssh-rsa"

    override fun getName(): String = "key"

    override fun isEncrypted(): Boolean = false

    override fun clear() = kpair.dispose()

}
