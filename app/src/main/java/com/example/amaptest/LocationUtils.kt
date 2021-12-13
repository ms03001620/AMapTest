package com.example.amaptest

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity


object LocationUtils {

    /**
     * 判断定位开关是否开启
     */
    fun isLocationSwitchOpen(context: Context): Boolean {
        return with(context.getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager) {
            // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
            val gps = this.isProviderEnabled(LocationManager.GPS_PROVIDER)
            // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
            val network = this.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            gps || network
        }
    }

    /**
     * 显示一个对话框，点去开启就尝试跳转到权限开关页面
     * @param context
     */
    fun goLocationServiceSettingForBluetooth(context: Context, leftCallback: (() -> Unit)? = null) {
        CommonAskDialog.Builder(
            context,
            context.getString(R.string.charging_open_settings),
            context.getString(R.string.cancel_scan),
            leftCallback = leftCallback
        ).create(
            context.getString(R.string.bluetooth_pair_permission_prompt), {
                try {
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                } catch (e: Exception) {
                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            })
    }


    fun goSettingForBluetooth(context: Context, leftCallback: (() -> Unit)? = null) {
        CommonAskDialog.Builder(
            context,
            context.getString(R.string.charging_open_settings),
            context.getString(R.string.cancel_option),
            leftCallback = leftCallback
        ).create(
            context.getString(R.string.bluetooth_pair_permission_prompt), {
                try {
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                } catch (e: Exception) {
                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            })
    }
}