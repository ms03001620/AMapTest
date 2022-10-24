package com.example.amaptest.flow

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FlowViewModel : ViewModel() {
    private val newsApi = NewsRepository()
    private val queueRepository = QueueRepository()
    val news = MutableLiveData<Int>()
    val itemString = MutableLiveData<String>()

    fun getNews() {
        viewModelScope.launch {
            newsApi.latestNews.collect {
                news.value = it
            }
            //newsApi.latestNews.
        }
    }


    fun getNewsOdd() {
        viewModelScope.launch {
            newsApi.latestNews.filter {
                it % 2 == 0
            }.catch {

            }.collect {
                news.value = it
            }
        }
    }

    fun linkToList() {
        viewModelScope.launch {
            queueRepository.itemsFlow.collect {
                itemString.value = it
            }
        }
    }

    fun addItems(s: String) {
        queueRepository.addItem(s)
    }
}