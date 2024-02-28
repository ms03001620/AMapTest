package com.polestar.store.widget

import org.junit.Assert.*
import org.junit.Test

class PointSplitTest {


    @Test
    fun splitPointHappy() {
        val result = PointSplit.splitPoint(50, 5)
        assertEquals(5, result.size)
        assertTrue(result.all { it == 10 })
    }

    @Test
    fun splitPointRemainder1() {
        val result = PointSplit.splitPoint(51, 5)

        assertEquals(5, result.size)
        assertEquals(4, result.count { it == 10 })
        assertEquals(11, result.last())
    }

    @Test
    fun splitPointRemainder4() {
        val result = PointSplit.splitPoint(54, 5)

        assertEquals(5, result.size)
        assertEquals(4, result.count { it == 10 })
        assertEquals(14, result.last())
    }


    @Test
    fun splitPointToSmall() {
        val result = PointSplit.splitPoint(4, 5)

        assertEquals(1, result.size)
        assertEquals(4, result.first())
    }
}