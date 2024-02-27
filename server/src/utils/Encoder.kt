package org.keizar.server.utils

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

interface Encoder {
    fun encode(data: String, secret: String): String
    fun decode(data: String, secret: String): String
}

class AesEncoder: Encoder {
    @OptIn(ExperimentalEncodingApi::class)
    override fun encode(data: String, secret: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val key = SecretKeySpec(secret.padEnd(32, ' ').toByteArray(), "AES")
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(ByteArray(16)))
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        return Base64.encode(encryptedBytes)
    }

    @OptIn(ExperimentalEncodingApi::class)
    override fun decode(data: String, secret: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val key = SecretKeySpec(secret.padEnd(32, ' ').toByteArray(), "AES")
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(ByteArray(16)))
        val decodedBytes = Base64.decode(data)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes)
    }
}
