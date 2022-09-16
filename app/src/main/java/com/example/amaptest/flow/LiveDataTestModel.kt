package com.example.amaptest.flow

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.amaptest.SingleLiveEvent

class LiveDataTestModel : ViewModel() {
    val data1 = SingleLiveEvent<Boolean>()
    val data2 = SingleLiveEvent<Boolean?>()
    val data3 = MutableLiveData<Boolean?>()
    val data4 = MutableLiveData<Boolean>()

    fun setAny() {
        data1.postValue(true)
        data2.postValue(null)
        data3.postValue(null)
        data4.postValue(true)
    }

    override fun onCleared() {
        Log.d("LiveDataActivity", "onCleared")
        super.onCleared()

    }
}