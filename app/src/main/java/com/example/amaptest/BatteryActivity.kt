package com.example.amaptest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.amaptest.databinding.ActivityBatteryBinding
import java.util.Locale

class BatteryActivity : AppCompatActivity() {
    private lateinit var batteryManager: BatteryManager
    private lateinit var binding: ActivityBatteryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBatteryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStart.setOnClickListener {

        }
        batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    }


    private fun updateBatteryInfo(intent: Intent, onMessage: ((String) -> Unit)?) {
        // 获取电池状态 (充电中, 已充满, 放电中等)
        val status: Int = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        // 获取充电类型 (AC, USB, 无线)
        val chargePlug: Int = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val isPlugged: Boolean = chargePlug == BatteryManager.BATTERY_PLUGGED_AC ||
                chargePlug == BatteryManager.BATTERY_PLUGGED_USB ||
                chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS

        // 获取当前电池电压 (单位: 毫伏 mV)
        val voltageMv: Int = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)

        // 获取当前瞬时电流 (单位: 微安 µA)
        // 注意: 充电时为正数, 放电时为负数。部分设备可能不支持或返回 0。
        val currentNowUa: Long = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

        // 准备用于显示/日志的文本
        val statusText: String
        var voltageText = "电压: N/A"
        var currentText = "电流: N/A"
        var powerText = "功率: N/A"
        val chargeTypeText = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_AC -> "充电类型: AC"
            BatteryManager.BATTERY_PLUGGED_USB -> "充电类型: USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "充电类型: 无线"
            else -> "充电类型: 未接入"
        }

        statusText = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "状态: 充电中"
            BatteryManager.BATTERY_STATUS_FULL -> "状态: 已充满"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "状态: 放电中"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "状态: 未充电"
            BatteryManager.BATTERY_STATUS_UNKNOWN -> "状态: 未知"
            else -> "状态: 未知 ($status)"
        }

        if (isCharging && isPlugged) { // 只有在接入电源且正在充电/已充满时计算功率才有意义
            if (voltageMv > 0) {
                val voltageV = voltageMv / 1000.0
                voltageText = String.format(Locale.getDefault(), "电压: %.2f V", voltageV)

                // 仅当电流为正数 (表示正在充电) 时计算功率
                if (currentNowUa > 0) {
                    val currentA = currentNowUa / 1_000_000.0
                    val powerW = voltageV * currentA
                    currentText = String.format(Locale.getDefault(), "电流: %.3f A", currentA) // 使用3位小数可能更精确
                    powerText = String.format(Locale.getDefault(), "功率: %.2f W", powerW)
                } else {
                    // 电流为 0 或负数，即使状态是 Charging (可能是涓流或不支持获取)
                    currentText = String.format(Locale.getDefault(), "电流: %.3f A (<=0)", currentNowUa / 1_000_000.0)
                    powerText = "功率: 无法计算 (电流 <= 0)"
                }
            } else {
                voltageText = "电压: N/A (无法获取)"
                currentText = "电流: N/A (无电压)"
                powerText = "功率: N/A (无电压)"
            }
        } else {
            // 未充电状态下，显示电压和可能的放电电流
            if (voltageMv > 0) {
                voltageText = String.format(Locale.getDefault(), "电压: %.2f V", voltageMv / 1000.0)
            }
            // 放电时 currentNowUa 为负数
            currentText = String.format(Locale.getDefault(), "电流: %.3f A", currentNowUa / 1_000_000.0)
            powerText = "功率: N/A (未充电)"
        }

        // 更新 UI 或打印日志
        val logMessage = "$statusText | $voltageText | $currentText | $powerText | $chargeTypeText"

        onMessage?.invoke(logMessage)

        println(logMessage)
    }


    private val batteryInfoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                updateBatteryInfo(intent){
                    val logs = binding.textContext.text.toString()
                    val newLog = it+"\n"+logs
                    binding.textContext.text = newLog
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryInfoReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(batteryInfoReceiver)
    }

    private fun getInitialBatteryStatus() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        // 获取最后一次的粘性广播 Intent
        val batteryStatusIntent = registerReceiver(null, filter)
        if (batteryStatusIntent != null) {
            updateBatteryInfo(batteryStatusIntent, null)
        }
    }
}