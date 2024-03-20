package com.polestar.base.ext

import org.junit.Assert.assertEquals
import org.junit.Test

class StringExKtTest {


    @Test
    fun stringToNumber() {
        assertEquals("0", "a".numberToString())
        assertEquals("0", "".numberToString())
        assertEquals("0", null.numberToString())
    }

    @Test
    fun simplyZero() {
        assertEquals("0", "0.0".numberToString())
        assertEquals("0", "0.00".numberToString())
    }

    @Test
    fun simply1() {
        assertEquals("1", "1.0".numberToString())
        assertEquals("1.1", "1.10".numberToString())
    }

    @Test
    fun simply45() {
        assertEquals("3.14", "3.141".numberToString())
        assertEquals("2.72", "2.71828".numberToString())
    }

    @Test
    fun simply451() {
        assertEquals("90", "90.000".numberToString())
        assertEquals("90", "90.0".numberToString())
    }
}