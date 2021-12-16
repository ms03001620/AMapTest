package com.example.amaptest.bluetooth.comp

import android.app.Activity
import android.bluetooth.BluetoothDevice
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

        override fun bindDevice(address: String?): Int {
            defStubStringDevice = "bindDevice"
            return 0
        }

        override fun cancelDiscovery(): Boolean {
            defStubStringDevice = "cancelDiscovery"
            return true
        }
    }


    @Before
    fun setup() {
        logic = BluetoothLogic( mockDevice, null, eventCenter)
    }

    @Test
    fun initTest() {
        assertEquals(TaskStep.SCAN, logic.getStep())
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
    fun scanStartTest() {
        var defStubStringUiCallback = ""
        logic.setUiCallback(object : BluetoothUiCallback {
            override fun onEvent(action: String) {
                defStubStringUiCallback = action
            }

            override fun onScanStart() {
                defStubStringUiCallback = "onScanStart"
            }
        })
        eventCenter.getCallback()?.onScanStart()
        assertEquals("onScanStart", defStubStringUiCallback)
    }

    @Test
    fun callbackFoundTest() {
        assertNull(eventCenter.address)
        eventCenter.getCallback()?.onFoundDevice()
        assertEquals(TaskStep.BIND, logic.getStep())
        assertEquals("cancelDiscovery", defStubStringDevice)
    }

    @Test
    fun callbackNotFoundTest() {
        var stubString = ""
        logic = BluetoothLogic( mockDevice, object : BluetoothUiCallback {
            override fun onNotFound(reasonCode: Int) {
                stubString = "onNotFound"
            }
        }, eventCenter)
        assertNull(eventCenter.address)

        eventCenter.address = ""
        eventCenter.getCallback()?.onFoundDevice()
        eventCenter.getCallback()?.onScanFinish()
        assertEquals(TaskStep.BIND, logic.getStep())
        assertEquals("", eventCenter.address)
        assertEquals("onNotFound", stubString)
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
                eventCenter.address = "aaa"
                eventCenter.getCallback()?.onFoundDevice()
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

            override fun bindDevice(address: String?): Int {
                stubString += "d"
                return 0
            }
        }

        logic = BluetoothLogic(mockDevice, object : BluetoothUiCallback {
            override fun requestPairing() {
                defStubStringUiCallback = "requestPairing"
            }

            override fun onBondedSuccess() {
                defStubStringUiCallback = "onBondedSuccess"
            }
        }, eventCenter)

        assertEquals(TaskStep.SCAN, logic.getStep())
        logic.doBluetoothTask()
        assertEquals(TaskStep.BIND, logic.getStep())

        eventCenter.getCallback()?.onScanFinish()
        //eventCenter.getCallback()?.onFoundDevice()
        assertEquals("abcd", stubString)

        //mock request pair
        eventCenter.getCallback()?.requestPairing()
        assertEquals("requestPairing", defStubStringUiCallback)

        //mock BOND_BONDED success
        eventCenter.getCallback()?.onBindStatusChange(BluetoothDevice.BOND_BONDING, BluetoothDevice.BOND_BONDED)
        assertEquals(TaskStep.BONDED, logic.getStep())
        assertEquals("onBondedSuccess", defStubStringUiCallback)
    }

    @Test
    fun doRetryBind() {
        var defStubStringUiCallback = ""
        logic = BluetoothLogic(mockDevice, object : BluetoothUiCallback {
            override fun onRequestReBinding() {
                defStubStringUiCallback = "onRequestReBinding"
            }
        }, eventCenter)

        eventCenter.getCallback()?.onBindStatusChange(BluetoothDevice.BOND_BONDING, BluetoothDevice.BOND_NONE)
        assertEquals(TaskStep.REQUEST_RETRY, logic.getStep())

        logic.doRetryBind()
        assertEquals(TaskStep.BIND, logic.getStep())
        assertEquals("bindDevice", defStubStringDevice)
        assertEquals("onRequestReBinding", defStubStringUiCallback)
    }

    @Test
    fun wasBonded() {
        var defStubStringUiCallback = ""
        logic = BluetoothLogic(object : BluetoothDevices {
            override fun bondedDevices(): Set<BluetoothDevice> = emptySet<BluetoothDevice>()

            override fun startDiscovery() = true

            override fun isDiscovering() = false

            override fun bindDevice(address: String?) = 12

            override fun cancelDiscovery() = true

        }, object : BluetoothUiCallback {
            override fun onBondedSuccess() {
                defStubStringUiCallback = "onBondedSuccess"
            }
        }, eventCenter)

        logic.doRetryBind()
        assertEquals("onBondedSuccess", defStubStringUiCallback)
        assertEquals(TaskStep.BONDED, logic.getStep())
    }

    @Test
    fun stop() {
        logic = BluetoothLogic(mockDevice, null, eventCenter)
        logic.stop()
        assertEquals("cancelDiscovery", defStubStringDevice)
    }

    @Test
    fun registerReceiver() {
        var stubString = ""
        logic = BluetoothLogic(mockDevice, null, object : ScanCenter() {
            override fun registerReceiver(activity: Activity?) {
                stubString = "registerReceiver"
            }
        })
        logic.registerReceiver(null)
        assertEquals("registerReceiver", stubString)
    }

    @Test
    fun unregisterReceiver() {
        var stubString = ""
        logic = BluetoothLogic(mockDevice, null, object : ScanCenter() {
            override fun unregisterReceiver(activity: Activity?) {
                stubString = "unregisterReceiver"
            }
        })
        logic.unregisterReceiver(null)
        assertEquals("unregisterReceiver", stubString)
    }

}