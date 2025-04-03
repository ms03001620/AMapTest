package com.example.amaptest.stateless

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class StateViewModel : ViewModel() {
    val checked = MutableLiveData<Boolean>(false)

    fun setCheckState(userRequestedState: Boolean) = viewModelScope.launch {
        checked.value = userRequestedState
    }

}