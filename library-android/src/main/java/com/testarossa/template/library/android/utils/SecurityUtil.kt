package com.testarossa.template.library.android.utils

import android.security.keystore.KeyProperties
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class SecurityUtil(secretKey: String) {
    private val bytesToStringSeparator = "|"

    private val cipher by lazy {
        Cipher.getInstance("AES/CBC/PKCS5PADDING")
    }
    private val secretKey by lazy {
        SecretKeySpec(secretKey.toByteArray(), KeyProperties.KEY_ALGORITHM_AES)
    }

    fun encryptToBytes(text: String): ByteArray {
        if (text.isEmpty()) return ByteArray(0)
        return try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(ByteArray(16)))
            cipher.doFinal(text.toByteArray())
        } catch (e: Exception) {
            ByteArray(0)
        }
    }

    fun encryptToString(text: String): String {
        val encryptedByteArray: ByteArray = encryptToBytes(text)
        return encryptedByteArray.joinToString(bytesToStringSeparator)
    }

    fun decryptData(encryptedData: ByteArray): String {
        val result = try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ByteArray(16)))
            cipher.doFinal(encryptedData)
        } catch (e: Exception) {
            ByteArray(0)
        }
        return String(result)
    }

    fun decryptData(encryptedData: String): String {
        return decryptData(encryptedData.split(bytesToStringSeparator).map { it.toByte() }
            .toByteArray())
    }
}
