package com.example.amaptest.flow

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class FlowViewModel : ViewModel() {
    private val newsApi = NewsRepository()
    private val queueRepository = QueueRepository()
    val news = MutableLiveData<Int>()
    val itemString = MutableLiveData<String>()
    val sharedFlow = MutableSharedFlow<Int>(replay = 2)

    init {
        getNewsOdd()
    }


    fun getNewsOdd() {
        viewModelScope.launch {
            newsApi.latestNews
                .flowOn(Dispatchers.IO)
                .filter {
                    it % 2 == 0
                }.catch {
                    it.printStackTrace()
                }.collect {
                    news.value = it
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("_____", "onCleared")
    }
}