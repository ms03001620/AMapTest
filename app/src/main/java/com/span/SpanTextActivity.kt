package com.span

import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.StrikethroughSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.amaptest.R
import com.polestar.base.ext.dp
import com.polestar.base.utils.numberToStringWithSign


class SpanTextActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_span_text)

        initSpanView1()
        initSpanView2()
        initSpanView3()

    }

    private fun initSpanView3() {
        val textSpan = findViewById<TextView>(R.id.textSpan2)
        val spannable: Spannable = SpannableString("ABCD")

        spannable.setSpan(
            DeleteLineSpan(),
            0,
            4,
            Spanned.SPAN_EXCLUSIVE_INCLUSIVE
        )

        textSpan.text = spannable
    }


    private fun initSpanView2() {
        val textSpan = findViewById<TextView>(R.id.textSpan1)
        val spannable: Spannable = SpannableString("ABCD")

        spannable.setSpan(
            StrikethroughSpan(),
            0,
            4,
            Spanned.SPAN_EXCLUSIVE_INCLUSIVE
        )

        textSpan.text = spannable
    }

    private fun initSpanView1() {
        val textSpan = findViewById<TextView>(R.id.textSpan)

        //  val string = "2020-18-20 12:15 最早"

        val sb = SpannableStringBuilder()
        sb.append("hello")
        sb.append(" ")
        sb.append(makeTagSpan("新增"))
        sb.append(" ")
        sb.append(
            makeTagSpan(
                text = "一般维修",
                textColor = Color.parseColor("#6C7075"),
                color = Color.parseColor("#ECECE7")
            )
        )

        val coup = 40f//source.statement?.coupons?.map { it.deductAmount ?: 0f }?.sum() ?: 0f
        val amount = 80f//source.pickUpInfo?.amount?.toFloat() ?: 0f
        val payAmount = amount - coup
       // val sb = SpannableStringBuilder()
        if (coup != 0f) {
            val string = coup.toString().numberToStringWithSign()
            SpannableStringBuilder(string).also {
                it.setSpan(
                    StrikethroughSpan(),
                    0,
                    string.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                sb.append(it)
                sb.append("  ")
            }
        }
        sb.append(payAmount.toString().numberToStringWithSign())



        textSpan.text = sb
    }

    private fun makeTagSpan(
        text: String?,
        textColor: Int = Color.WHITE,
        color: Int = Color.parseColor("#FF7500")

    ): CharSequence {
        if (text.isNullOrBlank()) return ""
        return SpannableString(text).also {
            it.setSpan(
                TagTextSpan(
                    scale = 0.6f,
                    bgMargin = Rect(5.dp, 3.dp, 6.dp, 5.dp),
                    textColor = textColor,
                    bgColor = color
                ),
                0,
                text.length,
                SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }


}

