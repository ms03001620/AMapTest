package com.example.amaptest.bluetooth.comp;

import android.bluetooth.BluetoothDevice;

import java.lang.reflect.Method;

public class UUU {

    public static boolean unpairDevice(BluetoothDevice device) {
        try {
            Method removeBondMethod = BluetoothDevice.class.getMethod("removeBond");
            Object result = removeBondMethod.invoke(device);
            if (result instanceof Boolean) {
                return (Boolean) result;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
