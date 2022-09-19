package com.example.amaptest.pager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.viewpager2.widget.ViewPager2
import com.example.amaptest.R

class PagerActivity : AppCompatActivity() {
    lateinit var pager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pager)
        initView()
        initPager()
    }

    fun initView() {
        pager = findViewById<ViewPager2>(R.id.view_pager)
    }

    fun initPager() {
        val adapter = VPAdapter(this)
        adapter.setData(dataArray)
        pager.adapter = adapter
        pager.orientation = ViewPager2.ORIENTATION_VERTICAL
        pager.offscreenPageLimit = 1
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Log.d("LiveFragment", "onPageSelected $position")
            }
        })
    }

    companion object {
        val dataArray = mutableListOf<String>().also {
            it.add("aaa")
            it.add("bbb")
            it.add("ccc")
            it.add("ddd")
            it.add("eee")
            it.add("fff")
            it.add("ggg")
            it.add("hhh")
            it.add("iii")
            it.add("jjj")
            it.add("kkk")
            it.add("lll")
            it.add("mmm")
            it.add("nnn")
            it.add("ooo")
            it.add("ppp")
            it.add("qqq")
            it.add("rrr")
            it.add("sss")
            it.add("ttt")
            it.add("uuu")
            it.add("vvv")
            it.add("www")
            it.add("xxx")
            it.add("yyy")
            it.add("zzz")
        }
    }
}