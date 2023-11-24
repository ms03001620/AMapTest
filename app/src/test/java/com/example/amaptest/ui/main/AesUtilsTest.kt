package com.example.amaptest.ui.main

import org.junit.Assert.assertEquals
import org.junit.Test
import javax.crypto.spec.SecretKeySpec

class AesUtilsTest {

    @Test
    fun genKey() {
        assertEquals(32, AesUtils.generateKey().encoded.size)
    }

    @Test
    fun encrypt() {
        val keyHex = "CF28819C1A750BB589D911E3F22FF16DBD31AAFA599E711B6642D9A98E68CCD5"
        val key = SecretKeySpec(AesUtils.hexToByte(keyHex), "AES")
        val p = AesUtils.encrypt("18600297222", key)
        assertEquals("782F705058A0F00F03A76C6BAF3607FB", AesUtils.byteToHex(p))
    }

    @Test
    fun decrypt() {
        val keyHex = "CF28819C1A750BB589D911E3F22FF16DBD31AAFA599E711B6642D9A98E68CCD5"
        val key = SecretKeySpec(AesUtils.hexToByte(keyHex), "AES")
        val p = AesUtils.decryptToStr(AesUtils.hexToByte("782F705058A0F00F03A76C6BAF3607FB"), key)
        assertEquals("18600297222", p)
    }

    @Test
    fun encryptAndDecrypt() {
        val phoneNumber = "18600297222"
        val key = AesUtils.generateKey()
        assertEquals(phoneNumber, AesUtils.decryptToStr(AesUtils.encrypt(phoneNumber, key), key))
    }

}