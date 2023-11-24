package com.example.amaptest

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.InputStream
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

//https://medium.com/@charanolati/asymmetric-encryption-using-rsa-algorithm-android-c9912ef0dacc
object RsaKeyGen {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "rsa_alias"

    private fun createAsymmetricKeyPair(): KeyPair {
        val generator =
            KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE)
        val builder = KeyGenParameterSpec.Builder(
            KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
            //.setUserAuthenticationRequired(true)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)

        generator.initialize(builder.build())

        return generator.generateKeyPair()
    }

    private fun getAsymmetricKeyPair(): KeyPair {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        val privateKey = keyStore.getKey(KEY_ALIAS, null) as PrivateKey?
        val publicKey = keyStore.getCertificate(KEY_ALIAS)?.publicKey

        return if (privateKey != null && publicKey != null) {
            KeyPair(publicKey, privateKey)
        } else {
            createAsymmetricKeyPair()
        }
    }

    /**
     * 生成公钥，从外部输入
     */
    fun createPublicKey(stream: InputStream): PublicKey {
        val key =  stream.bufferedReader().use { it.readText() }
        val publicKeyPEM = key
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace(System.lineSeparator().toRegex(), "")
            .replace("-----END PUBLIC KEY-----", "")
        val encoded = decodeBase64(publicKeyPEM)
        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = X509EncodedKeySpec(encoded)
        return keyFactory.generatePublic(keySpec) as RSAPublicKey
    }

    /**
     * 生成私钥，从外部输入
     */
    fun createPrivateKey(stream: InputStream): PrivateKey {
        val key =  stream.bufferedReader().use { it.readText() }
        val publicKeyPEM = key
            .replace("-----BEGIN RSA PRIVATE KEY-----", "")
            .replace(System.lineSeparator().toRegex(), "")
            .replace("-----END RSA PRIVATE KEY-----", "")
        val encoded =
            decodeBase64(publicKeyPEM)
        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = PKCS8EncodedKeySpec(encoded)
        return keyFactory.generatePrivate(keySpec) as PrivateKey
    }

    fun encrypt(data: String, publicKey: PublicKey = getAsymmetricKeyPair().public): String {
        val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val bytes = cipher.doFinal(data.toByteArray())
        return base64encodeToString(bytes)
    }



    fun decrypt(data: String, privateKey: PrivateKey = getAsymmetricKeyPair().private): String {
        val cipher: Cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val encryptedData = decodeBase64(data)
        val decodedData = cipher.doFinal(encryptedData)
        return String(decodedData)
    }

    private fun decodeBase64(string: String): ByteArray? =
        android.util.Base64.decode(string, android.util.Base64.DEFAULT)
        //java.util.Base64.getDecoder().decode(string)

    private fun base64encodeToString(bytes: ByteArray?): String =
        android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
        //java.util.Base64.getEncoder().encodeToString(bytes)

}
