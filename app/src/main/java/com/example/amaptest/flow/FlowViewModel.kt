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
    val news = MutableLiveData<Int>()

    fun getNews() {
        viewModelScope.launch {
            newsApi.latestNews.collect {
                news.value = it
            }
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

}