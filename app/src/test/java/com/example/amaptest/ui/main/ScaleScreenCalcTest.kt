package com.example.amaptest.ui.main

import android.graphics.Point
import android.graphics.Rect
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ScaleScreenCalcTest {

    @Test
    fun textScaleToScreen() {
        ScaleScreenCalc.mapScaleToScreen(
            scaleRect = Rect(
                0,
                0,
                100,
                100
            ),
            targetSize = Point(100, 100),
            canvasSize = Point(200, 200)
        ).let {
            assertEquals(0, it.left)
            assertEquals(0, it.top)
            assertEquals(200, it.right)
            assertEquals(200, it.bottom)
        }
    }

    @Test
    fun testScreenToScale() {
        ScaleScreenCalc.mapScreenToScale(
            screen = Rect(
                0,
                0,
                200,
                200
            ),
            targetSize = Point(100, 100),
            canvasSize = Point(200, 200)
        ).let {
            assertEquals(0, it.left)
            assertEquals(0, it.top)
            assertEquals(100, it.right)
            assertEquals(100, it.bottom)
        }
    }

    @Test
    fun testScreenToScale704x576() {
        ScaleScreenCalc.mapScreenToScale(
            screen = Rect(
                0,
                0,
                1280,
                960
            ),
            targetSize = Point(704, 576),
            canvasSize = Point(1280, 960)
        ).let {
            assertEquals(0, it.left)
            assertEquals(0, it.top)
            assertEquals(704, it.right)
            assertEquals(576, it.bottom)
        }
    }
}