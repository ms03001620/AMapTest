package com.polestar.store.widget

object PointSplit {
    fun splitPoint(point: Int, size: Int = 5): List<Int> {
        val result = ArrayList<Int>()

        if (point < size) {
            result.add(point)
            return result
        }

        val unit = point / size
        val remainder = point % size

        for (i in 0 until size - 1) {
            result.add(unit)
        }
        result.add(unit + remainder)

        return result
    }
}