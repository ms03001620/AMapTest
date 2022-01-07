package com.polestar.charging.ui.station.plate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amaptest.SingleLiveEvent
import com.polestar.base.utils.loge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VehiclesViewModel : ViewModel() {
    val vehiclesLiveData = MutableLiveData<List<Plate>?>()
    val plateLiveData = SingleLiveEvent<Plate?>()

    fun getVehicles() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                delay(1000)
                val data = convert()
                vehiclesLiveData.postValue(data)
            } catch (e: Exception) {
                loge("getVehicles", TAG, e)
                vehiclesLiveData.postValue(emptyList())
            }
        }
    }

    var vin: String? = ""

    fun selectPlate(plate: Plate) {
        plateLiveData.value = plate
        vin = plate.vin
    }

    fun saveDefaultVin(vin: String?) {
        this.vin = vin
    }

    fun loadDefaultVin() = vin

    fun loadDefVin(plates: List<Plate>): String? {
        var defVin = loadDefaultVin()
        if (defVin == null) {
            defVin = plates.firstOrNull()?.vin
            if (defVin != null) {
                saveDefaultVin(defVin)
            }
        }
        return defVin
    }

    fun convert(): List<Plate> {
        val result = mutableListOf<Plate>()

        // TODO remove mock
        result.add(Plate("警AB1234", "LYVPKBDTDLB000081"))
        result.add(Plate("a", "LYVPKBDTDLB000082"))
        result.add(Plate("", "LYVPKBDTDLB000083"))
        //result.add(Plate("01234567890123456789012345678901234567890123456789", "LYVPKBDTDLB000084"))
        // result.add(Plate("01234567890123", "LYVPKBDTDLB000085"))
        return result.toList()
    }

    fun getPlateList() = vehiclesLiveData.value ?: emptyList()

    fun setMockData(list: List<Plate>) {
        vehiclesLiveData.value = list
    }

    companion object {
        const val TAG = "VehiclesViewModel"
    }
}