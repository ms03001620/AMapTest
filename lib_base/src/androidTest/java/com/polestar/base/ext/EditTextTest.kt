package com.polestar.base.ext

import android.text.InputFilter
import android.util.Range
import android.widget.EditText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditTextExtTest {

    private lateinit var editText: EditText

    @Before
    fun setUp() {
        editText = EditText(ApplicationProvider.getApplicationContext())
        editText.setupFilter(InputFilter.LengthFilter(5), RangeFilter(Range(0, 100)))
    }

    @Test
    fun test1() {

    }


}
