package com.example.amaptest.bluetooth.comp

import android.bluetooth.BluetoothDevice
import com.example.amaptest.bluetooth.BluetoothHardware
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BluetoothLogicTest {
    val eventCenter = ScanCenter()
    lateinit var logic: BluetoothLogic

    var callbackString = ""

    val callbackUi = object : BluetoothCallback {
        override fun onEvent(action: String) {
            callbackString = action
        }

        override fun onFoundDevice(address: String) {
            callbackString = address
        }
    }

    val mockDevice = object : BluetoothHardware {
        override fun bondedDevices(): Set<BluetoothDevice> {
            return emptySet<BluetoothDevice>()
        }

        override fun startDiscovery(): Boolean {
            return true
        }

        override fun isDiscovering(): Boolean {
            return true
        }
    }


    @Before
    fun setup() {
        callbackString = ""
        logic = BluetoothLogic("mockName", mockDevice, callbackUi, eventCenter)
    }

    @Test
    fun initTest() {
        assertEquals(TaskStep.SCAN, logic.step)
    }

    @Test
    fun callbackBaseTest() {
        eventCenter.getCallback()?.onEvent("abc")
        assertEquals("abc", callbackString)
    }

    @Test
    fun callbackFoundTest() {
        assertNull(eventCenter.address)
        eventCenter.getCallback()?.onFoundDevice("0F:01")
        assertEquals(TaskStep.BIND, logic.step)
        assertEquals("0F:01", eventCenter.address)
    }


}