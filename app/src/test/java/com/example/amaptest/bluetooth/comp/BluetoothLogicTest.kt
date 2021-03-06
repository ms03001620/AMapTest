package com.example.amaptest.bluetooth.comp

import android.app.Activity
import android.bluetooth.BluetoothDevice
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.lang.Exception

class BluetoothLogicTest {
    private val eventCenter = OnScanEvent()
    private lateinit var logic: BluetoothLogic

    private var defStubStringDevice = ""

    private val mockDevice = object : BluetoothDevices {
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
        logic = BluetoothLogic(mockDevice, null, eventCenter)
    }

    @Test
    fun initTest() {
        assertEquals(TaskStep.SCAN, logic.getStep())
    }

    @Test
    fun scanStartTest() {
        var defStubStringUiCallback = ""
        logic.setUiCallback(object : OnScanEventCallback {
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
        assertEquals("cancelDiscovery", defStubStringDevice)
    }

    @Test
    fun callbackNotFoundTest() {
        var stubString = ""
        logic = BluetoothLogic(mockDevice, object : OnScanEventCallback {
            override fun onNotFound(reasonCode: Int) {
                stubString = "onNotFound"
            }
        }, eventCenter)
        assertNull(eventCenter.address)

        eventCenter.address = ""
        eventCenter.getCallback()?.onFoundDevice()
        eventCenter.getCallback()?.onScanFinish()
        assertEquals(TaskStep.SCAN, logic.getStep())
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

        logic = BluetoothLogic(mockDevice, object : OnScanEventCallback {
            override fun onRequestPairing() {
                defStubStringUiCallback = "requestPairing"
            }

            override fun onBondedSuccess(macAddress: String?) {
                defStubStringUiCallback = "onBondedSuccessaaa"
            }
        }, eventCenter)

        assertEquals(TaskStep.SCAN, logic.getStep())
        logic.doBluetoothTask()

        eventCenter.getCallback()?.onScanFinish()
        assertEquals(TaskStep.BIND, logic.getStep())
        //eventCenter.getCallback()?.onFoundDevice()
        assertEquals("abcd", stubString)

        //mock request pair
        eventCenter.getCallback()
            ?.onBindStatusChange(BluetoothDevice.BOND_NONE, BluetoothDevice.BOND_BONDING)
        assertEquals("requestPairing", defStubStringUiCallback)

        //mock BOND_BONDED success
        eventCenter.getCallback()
            ?.onBindStatusChange(BluetoothDevice.BOND_BONDING, BluetoothDevice.BOND_BONDED)
        assertEquals("onBondedSuccessaaa", defStubStringUiCallback)
    }

    @Test
    fun doRetryBind() {
        var defStubStringUiCallback = ""
        logic = BluetoothLogic(mockDevice, object : OnScanEventCallback {
            override fun onRequestReBinding() {
                defStubStringUiCallback = "onRequestReBinding"
            }
        }, eventCenter)

        eventCenter.getCallback()
            ?.onBindStatusChange(BluetoothDevice.BOND_BONDING, BluetoothDevice.BOND_NONE)
        assertEquals(TaskStep.BIND, logic.getStep())

        logic.doBluetoothTask()
        assertEquals("bindDevice", defStubStringDevice)
        assertEquals("onRequestReBinding", defStubStringUiCallback)
    }

    @Test
    fun doRetryScan() {
        var stubString = ""
        logic = BluetoothLogic(mockDevice, object : OnScanEventCallback {
            override fun onNotFound(reasonCode: Int) {
                stubString = "onNotFound"
            }
        }, eventCenter)
        assertNull(eventCenter.address)

        eventCenter.address = ""
        eventCenter.getCallback()?.onFoundDevice()
        eventCenter.getCallback()?.onScanFinish()
        assertEquals(TaskStep.SCAN, logic.getStep())
        assertEquals("", eventCenter.address)
        assertEquals("onNotFound", stubString)

        logic.doBluetoothTask()
        assertEquals(TaskStep.SCAN, logic.getStep())
        assertEquals("isDiscovering", defStubStringDevice)
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

        }, object : OnScanEventCallback {
            override fun onBondedSuccess(macAddress: String?) {
                defStubStringUiCallback = "onBondedSuccess"
            }
        }, eventCenter)

        eventCenter.address = "abc"
        eventCenter.getCallback()?.onScanFinish()
        logic.doBluetoothTask()
        assertEquals("onBondedSuccess", defStubStringUiCallback)
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
        logic = BluetoothLogic(mockDevice, null, object : OnScanEvent() {
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
        logic = BluetoothLogic(mockDevice, null, object : OnScanEvent() {
            override fun unregisterReceiver(activity: Activity?) {
                stubString = "unregisterReceiver"
            }
        })
        logic.unregisterReceiver(null)
        assertEquals("unregisterReceiver", stubString)
    }

    @Test
    fun doBluetoothTaskScanException() {
        var stubString = ""
        logic = BluetoothLogic(object : BluetoothDevices {
            override fun bondedDevices(): Set<BluetoothDevice> {
                return emptySet<BluetoothDevice>()
            }

            override fun startDiscovery(): Boolean {
                throw Exception("mock error")
            }

            override fun isDiscovering(): Boolean {
                return false
            }

            override fun bindDevice(address: String?): Int {
                return 0
            }

            override fun cancelDiscovery(): Boolean {
                return true
            }
        }, object : OnScanEventCallback {
            override fun onNotFound(reasonCode: Int) {
                stubString = "onNotFound"
                super.onNotFound(reasonCode)
            }

        }, eventCenter)

        logic.doBluetoothTask()
        assertEquals("onNotFound", stubString)
    }

    @Test
    fun doBluetoothTaskBindingException() {
        var stubString = ""
        logic = BluetoothLogic(object : BluetoothDevices {
            override fun bondedDevices(): Set<BluetoothDevice> {
                return emptySet<BluetoothDevice>()
            }

            override fun startDiscovery(): Boolean {
                return true
            }

            override fun isDiscovering(): Boolean {
                return false
            }

            override fun bindDevice(address: String?): Int {
                throw Exception("mock exception")
            }

            override fun cancelDiscovery(): Boolean {
                return true
            }
        }, object : OnScanEventCallback {
            override fun onRequestReBinding() {
                stubString = "onRequestReBinding"
            }
        }, eventCenter)

        eventCenter.address = "abc"
        eventCenter.getCallback()?.onScanFinish()
        assertEquals(TaskStep.BIND, logic.getStep())
        logic.doBluetoothTask()
        assertEquals("onRequestReBinding", stubString)
    }

}