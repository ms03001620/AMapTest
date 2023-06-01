package com.example.amaptest.sync

import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class CellSignalModel : ViewModel() {
    val signalStrengthLiveData = MutableLiveData<Int?>()

    fun reg(context: Context) {
        Log.d("CellSignalModel", "reg")
        context.getSystemService(TelephonyManager::class.java).apply {
            signalStrengthLiveData.postValue(null)
            this.listen(listener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
        }
    }

    fun unReg(context: Context) {
        Log.d("CellSignalModel", "unReg")
        context.getSystemService(TelephonyManager::class.java).apply {
            this.listen(listener, PhoneStateListener.LISTEN_NONE)
            signalStrengthLiveData.postValue(null)
        }
    }

    private val listener = object : PhoneStateListener() {
        @Deprecated("Deprecated in Java")
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
            super.onSignalStrengthsChanged(signalStrength)
            Log.d("CellSignalModel", "signalStrength level：" + signalStrength.level)
            signalStrengthLiveData.postValue(signalStrength.level)
        }
    }
}