package com.example.amaptest.ui.main

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


object AesUtils {
    fun generateKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        val secureRandom = SecureRandom()
        keyGenerator.init(256, secureRandom)
        return keyGenerator.generateKey()
    }

    fun encrypt(content: String, secretKey: SecretKey): ByteArray {
        val enCodeFormat = secretKey.encoded
        return encrypt(content, enCodeFormat)
    }

    fun encrypt(content: String, secretKeyEncoded: ByteArray?): ByteArray {
        val key = SecretKeySpec(secretKeyEncoded, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(ByteArray(cipher.blockSize)))
        return cipher.doFinal(content.toByteArray(StandardCharsets.UTF_8))
    }

    fun decrypt(content: ByteArray?, secretKeyEncoded: ByteArray?): ByteArray {
        val key = SecretKeySpec(secretKeyEncoded, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(ByteArray(cipher.blockSize)))
        return cipher.doFinal(content)
    }

    fun decrypt(content: ByteArray?, secretKey: SecretKey): ByteArray {
        val enCodeFormat = secretKey.encoded
        return decrypt(content, enCodeFormat)
    }

    fun decryptToStr(content: ByteArray?, secretKey: SecretKey): String {
        val result = decrypt(content, secretKey)
        return String(result)
    }

    fun createPublicKeyAndEncryptRandomAesKey(keyData: ByteArray): Pair<ByteArray, ByteArray> {
        val publicKey = createPublicKey(keyData)
        val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val aesKeyData = generateKey().encoded
        val rsaAes = cipher.doFinal(aesKeyData)
        return Pair(rsaAes, aesKeyData)
    }

    fun createPublicKey(keyData: ByteArray): PublicKey? {
        try {
            val X509publicKey = X509EncodedKeySpec(keyData)
            val kf = KeyFactory.getInstance("RSA")
            return kf.generatePublic(X509publicKey)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun importPubKey(publicKey: String?): PublicKey? {
        var pubKey: PublicKey? = null //  www  .  jav  a  2 s.c o  m
        if (publicKey != null) {
            try {
                val keyFactory = KeyFactory.getInstance("RSA")
                val keySpec = X509EncodedKeySpec(
                    Base64.decode(publicKey.toByteArray(), Base64.NO_WRAP)
                )
                pubKey = keyFactory.generatePublic(keySpec)
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: InvalidKeySpecException) {
                e.printStackTrace()
            }
        }
        return pubKey
    }


/*
    fun bytesToBase64(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.DEFAULT)
    }

    fun base64ToBytes(base64: String): ByteArray {
        return Base64.decode(base64, Base64.DEFAULT)
    }
*/

    fun hexToByte(s: String): ByteArray {
        check(s.length % 2 == 0) { "Must have an even length" }

        return s.chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    fun byteToHex(byteArray: ByteArray) =
        byteArray.joinToString(separator = "") { eachByte -> "%02x".format(eachByte).uppercase() }
}