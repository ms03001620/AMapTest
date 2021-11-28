package com.example.amaptest.ui.main

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amaptest.AssetsReadUtils
import com.example.amaptest.SingleLiveEvent
import com.polestar.repository.data.charging.StationDetail
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    val stationLiveData = MutableLiveData<StationDetail>()
    val stationListLiveData = MutableLiveData<List<StationDetail>>()
    val markerCollapsedLiveData = SingleLiveEvent<Boolean>()
    val locationLiveData = MutableLiveData<Boolean>()
    var markerExpandedCallback: ((StationDetail?) -> Unit)? = null

    fun startLocation() {
        locationLiveData.postValue(true)
    }

    fun setMarkerCollapsed() {
        markerCollapsedLiveData.postValue(true)
    }

}