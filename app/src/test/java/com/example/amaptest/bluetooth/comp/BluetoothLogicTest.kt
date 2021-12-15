package com.example.amaptest.bluetooth.comp

import android.bluetooth.BluetoothDevice
import com.example.amaptest.bluetooth.BluetoothDevices
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class BluetoothLogicTest {
    val eventCenter = ScanCenter()
    lateinit var logic: BluetoothLogic

    var defStubStringDevice = ""


    val mockDevice = object : BluetoothDevices {
        override fun bondedDevices(): Set<BluetoothDevice> {
            defStubStringDevice = "bondedDevices"
            return emptySet<BluetoothDevice>()
        }

        override fun startDiscovery(): Boolean {
            defStubStringDevice = "startDiscovery"
            return true
        }

        override fun isDiscovering(): Boolean {
            defStubStringDevice = "isDiscovering"
            return true
        }

        override fun bindDevice(address: String?): Boolean {
            defStubStringDevice = "bindDevice"
            return true
        }

        override fun cancelDiscovery(): Boolean {
            defStubStringDevice = "cancelDiscovery"
            return true
        }
    }


    @Before
    fun setup() {
        logic = BluetoothLogic("mockName", 6, mockDevice, null, eventCenter)
    }

    @Test
    fun initTest() {
        assertEquals(TaskStep.SCAN, logic.step)
    }

    @Test
    fun callbackBaseTest() {
        var defStubStringUiCallback = ""
        logic.setUiCallback(object:BluetoothUiCallback{
            override fun onEvent(action: String) {
                defStubStringUiCallback = action
            }
        })
        eventCenter.getCallback()?.onEvent("abc")
        assertEquals("abc", defStubStringUiCallback)
    }

    @Test
    fun callbackFoundTest() {
        assertNull(eventCenter.address)
        eventCenter.getCallback()?.onFoundDevice("0F:01")
        assertEquals(TaskStep.BIND, logic.step)
        assertEquals("0F:01", eventCenter.address)
    }

    @Test
    fun integrationTestHappyPath() {
        var defStubStringUiCallback = ""
        var stubString = ""
        val mockDevice = object : BluetoothDevices {
            override fun bondedDevices(): Set<BluetoothDevice> {
                return emptySet<BluetoothDevice>()
            }

            override fun startDiscovery(): Boolean {
                stubString += "b"
                // mock found device immediately
                eventCenter.getCallback()?.onFoundDevice("0F:02")
                return true
            }

            override fun isDiscovering(): Boolean {
                stubString += "a"
                return false
            }

            override fun cancelDiscovery(): Boolean {
                stubString += "c"
                return true
            }

            override fun bindDevice(address: String?): Boolean {
                stubString += "d"
                return true
            }

        }

        logic = BluetoothLogic("mockName", 6, mockDevice, object : BluetoothUiCallback {
            override fun requestPairing() {
                defStubStringUiCallback = "requestPairing"
            }
        }, eventCenter)

        assertEquals(TaskStep.SCAN, logic.step)
        logic.doBluetoothTask()
        assertEquals(TaskStep.BIND, logic.step)
        assertEquals("0F:02", eventCenter.address)
        eventCenter.getCallback()?.onScanFinish()
        assertEquals("abcd", stubString)

        //mock request pair
        eventCenter.getCallback()?.requestPairing()
        assertEquals("requestPairing", defStubStringUiCallback)
    }


}