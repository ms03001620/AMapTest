package com.span

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.BackgroundColorSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.amaptest.R


class SpanTextActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_span_text)

        val textSpan = findViewById<TextView>(R.id.textSpan)

        val string = "2022020-18-20 2020-18-20 2020-18-20 0-18-20 2020-18-20 2020-18-20 12:15 最早"

        val spannable: Spannable = SpannableString(string)
        spannable.setSpan(
            BackgroundColorSpan(Color.GRAY),
            string.length - 2,
            string.length,
            SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            TagTextSpan(),
            string.length - 2,
            string.length,
            SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textSpan.text = spannable
    }
}