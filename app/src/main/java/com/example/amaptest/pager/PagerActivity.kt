package com.example.amaptest.pager

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewStub
import androidx.core.view.GestureDetectorCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.amaptest.R

class PagerActivity : AppCompatActivity() {
    lateinit var pager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ViewPager2", "PagerActivity onCreate")
        setContentView(R.layout.activity_pager)
        initView()
        initPager()
    }

    private fun initInterceptor(){
        val layoutInterceptor = findViewById<ViewStub>(R.id.stub_scroll_Interceptor).inflate()

        pager.isUserInputEnabled = false
        val detector = GestureDetectorCompat(layoutInterceptor.context, UpDownFlingListener {
            //ToastUtil.show("aaaa")
        })


//        GestureDetectorHelper(layoutInterceptor, object : OnGesturePageEvent {
//            override fun canUnLockGesture(finger: Finger): Boolean {
//                when (finger) {
//                    Finger.DOWN, Finger.UP -> {
//                        DialogUtil.confirmDialog(mContext, "", "连麦中无法滑动直播间哦", false, object : DialogUtil.Callback {
//                            override fun confirm(dialog: Dialog) {
//                                dialog.dismiss()
//                            }
//
//                            override fun cancel(dialog: Dialog) {
//                                dialog.dismiss()
//                            }
//                        }).show()
//
//                    }
//                }
//                return false
//            }
//
//            override fun onUnLock() {
//                viewPager.isUserInputEnabled = true
//            }
//
//            override fun onLock() {
//                viewPager.isUserInputEnabled = false
//            }
//        })
    }


    fun initView() {
        pager = findViewById<ViewPager2>(R.id.view_pager)
    }

    var currentPageIndex = 0

    fun initPager() {
        val adapter = VPAdapter(this)
        adapter.setData(dataArray)
        pager.adapter = adapter
        pager.orientation = ViewPager2.ORIENTATION_VERTICAL
        pager.offscreenPageLimit = 1
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPageIndex = position
                Log.d("LiveFragment", "onPageSelected $position")
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d("ViewPager2", "PagerActivity onConfigurationChanged:")
        applyFixSmoothError(newConfig)
    }

    /**
     * 引用修复横竖屏切换后viewpager的页面滚动位置错误
     * https://issuetracker.google.com/issues/175796502?pli=1
     */
    private fun applyFixSmoothError(newConfig: Configuration) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val c = currentPageIndex
            pager.setCurrentItem(if (currentPageIndex - 1 > 0) currentPageIndex - 1 else 0, false)
            pager.setCurrentItem(c, false)
        }
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