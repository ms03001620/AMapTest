package com.example.amaptest.life

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.example.amaptest.R

class LifecycleObserverActivity : AppCompatActivity(), LifecycleOwner {
    val stringLiveData = MutableLiveData<String>("abc")
    val observer = MyObserver(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lifecycler_observer)
        lifecycle.addObserver(observer)
        stringLiveData.observe(/*observer.getOwner()*/this) {
            Log.d("_____", "msg:$it")
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("_____", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("_____", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("_____", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("_____", "onStop")
    }
}