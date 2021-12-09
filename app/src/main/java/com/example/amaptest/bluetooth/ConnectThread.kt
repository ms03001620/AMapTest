package com.example.amaptest.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.lang.Exception
import java.util.*

class ConnectThread(device: BluetoothDevice, uuid: UUID) : Thread() {
    private val socket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
        device.createRfcommSocketToServiceRecord(uuid)
    }

    override fun run() {
        socket?.use { socket ->
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            try {
                Log.d("ConnectThread", "connect")
                socket.connect()
                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                Log.d("ConnectThread", "connect:$socket")
            } catch (e: Exception) {
                Log.e("ConnectThread", "connect:", e)
            }finally {
                cancel()
            }
        }
    }

    // Closes the client socket and causes the thread to finish.
    fun cancel() {
        try {
            socket?.close()
        } catch (e: IOException) {
            Log.e("ConnectThread", "Could not close the client socket", e)
        }
    }
}