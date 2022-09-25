package net.thatapex.shellbot.ssh.key

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMReader
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.Security
import java.security.interfaces.RSAPublicKey
import java.util.*


object SSHKeyUtils {

    init {
        Security.addProvider(BouncyCastleProvider())
    }

    fun generate(keySize: Int = 2048): KeyPair {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(keySize)
        return generator.genKeyPair()
    }

    fun load(stream: InputStream): KeyPair {
        return PEMReader(InputStreamReader(stream)).use {
            it.readObject() as KeyPair
        }
    }

    fun encodeAsOpenSSH(key: RSAPublicKey): String {
        val keyBlob: ByteArray = keyBlob(key.publicExponent, key.modulus)
        val encodedByteArray: ByteArray = Base64.getEncoder().encode(keyBlob)
        val encodedString = String(encodedByteArray)
        return "ssh-rsa $encodedString"
    }

    private fun keyBlob(publicExponent: BigInteger, modulus: BigInteger): ByteArray {
        val out = ByteArrayOutputStream()
        writeLengthFirst("ssh-rsa".toByteArray(), out)
        writeLengthFirst(publicExponent.toByteArray(), out)
        writeLengthFirst(modulus.toByteArray(), out)
        return out.toByteArray()
    }

    private fun writeLengthFirst(array: ByteArray, out: ByteArrayOutputStream) {
        out.write(array.size ushr 24 and 0xFF)
        out.write(array.size ushr 16 and 0xFF)
        out.write(array.size ushr 8 and 0xFF)
        out.write(array.size ushr 0 and 0xFF)
        if (array.size == 1 && array[0] == 0x00.toByte()) out.write(ByteArray(0)) else out.write(array)
    }
}
