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

    @Test
    fun testUUID() {
        val uuid = UUID.nameUUIDFromBytes("Hello".toByteArray(Charsets.UTF_8))

        assertEquals("8b1a9953-c461-3296-a827-abf8c47804d7", uuid.toString())

        assertEquals(
            UUID.fromString("8b1a9953-c461-3296-a827-abf8c47804d7").toString(),
            uuid.toString()
        )
    }

    @Test
    fun papterTile() {
        assertEquals(5, calcSameBitAtTile("pc12345", "abcd12345"))
        assertEquals(3, calcSameBitAtTile("b123", "a123"))
        assertEquals(2, calcSameBitAtTile("12", "312"))
        assertEquals(1, calcSameBitAtTile("abcd1", "efgk1"))
        assertEquals(5, calcSameBitAtTile("abcdef", "bcdef"))

        assertEquals(0, calcSameBitAtTile("1234", "abcd"))
        assertEquals(0, calcSameBitAtTile("1234", ""))
        assertEquals(0, calcSameBitAtTile("", "1234"))
        assertEquals(0, calcSameBitAtTile("12345678", "1234567"))
        assertEquals(0, calcSameBitAtTile(null, null))
    }


    fun calcSameBitAtTile(source: String?, target: String?): Int {
        if (source.isNullOrBlank() || target.isNullOrBlank()) {
            return 0
        }
        var count = 0

        var sourceLastIndex = source.lastIndex
        var targetLastIndex = target.lastIndex

        while (sourceLastIndex >= 0 && targetLastIndex >= 0) {
            if (source[sourceLastIndex] == target[targetLastIndex]) {
                count++
                sourceLastIndex--
                targetLastIndex--
            } else {
                break
            }
        }
        return count
    }
}