package com.example.amaptest.bluetooth

import junit.framework.Assert.*
import org.junit.Test
import java.util.*


class BaseTest {


    @Test
    fun baseStringTest() {
        val string = "3c:06:30:19:c2:DA"

        val string1 = "3C:06:30:19:C2:DA"

        assertNotSame(string, string1)
        assertTrue(string.equals(string1, ignoreCase = true))
    }

    @Test
    fun baseStringTest2() {
        val map = hashMapOf<String, String>()
        map.put("3c:06:30:19:c2:DA", "1")
        val string1 = "3C:06:30:19:C2:DA"

        assertFalse(map.containsKey(string1))
        assertEquals("", "".uppercase())
    }
}