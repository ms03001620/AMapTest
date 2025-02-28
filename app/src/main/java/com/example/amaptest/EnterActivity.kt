package com.example.amaptest

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.amap.api.maps.MapsInitializer
import com.example.amaptest.ble.BleActivity
import com.example.amaptest.bluetooth.BluetoothActivity
import com.example.amaptest.bluetooth.BluetoothPermissionHelper
import com.example.amaptest.bluetooth.BluetoothSampleActivity
import com.example.amaptest.carctrl.CarCtrlActivity
import com.example.amaptest.eventbus.EventbusCreateActivity
import com.example.amaptest.floatlist.FloatListActivity
import com.example.amaptest.flow.FlowActivity
import com.example.amaptest.flow.FlowAvdActivity
import com.example.amaptest.flow.LiveDataActivity
import com.example.amaptest.header.HeaderActivity
import com.example.amaptest.keyboard.KeyboardActivity
import com.example.amaptest.life.LifecycleObserverActivity
import com.example.amaptest.pager.PagerActivity
import com.example.amaptest.rect.RectActivity
import com.example.amaptest.rect.RectAreaActivity
import com.example.amaptest.sync.CellSignalModel
import com.robolectric.DialogsActivity
import com.span.SpanTextActivity


class EnterActivity : AppCompatActivity() {


    private val viewModel by lazy {
        ViewModelProvider(this, ViewModelFactory())[CellSignalModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter)
        viewModel.reg(this)


        // 设置状态栏
        findViewById<View>(R.id.btn_statusbar).setOnClickListener {
            startActivity(Intent(this, BarColorSetActivity::class.java))
        }

        //RoWel
        findViewById<View>(R.id.btn_countdown).setOnClickListener {
            startActivity(Intent(this, CountdownActivity::class.java))
        }

        findViewById<TextView>(R.id.text_version_b).apply {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            val version = Build.VERSION.SDK_INT
            val versionRelease = Build.VERSION.RELEASE
            val channel = BuildConfig.CHANNEL
            this.text = "manufacturer $manufacturer model $model version $version versionRelease $versionRelease channel $channel"
        }

        findViewById<View>(R.id.btn_dialog).setOnClickListener {
            startActivity(Intent(this, DialogsActivity::class.java))
        }

        findViewById<View>(R.id.btn_rect).setOnClickListener {
            gotoRectActivity()
        }

        findViewById<View>(R.id.btn_detect_area).setOnClickListener {
            gotoRectAreaActivity()
        }

        // 进入高德地图
        findViewById<View>(R.id.btn_map).setOnClickListener {
            MapsInitializer.updatePrivacyAgree(applicationContext, true)
            gotoAMap()
        }

        findViewById<View>(R.id.btn_pager).setOnClickListener {
            gotoPager()
        }

        findViewById<View>(R.id.btn_keyboard).setOnClickListener {
            gotoKeyboardActivity()
        }

        findViewById<View>(R.id.btn_anim).setOnClickListener {
            gotoAnim()
        }

        findViewById<View>(R.id.btn_eventBus).setOnClickListener {
            gotoEventBus()
        }

        findViewById<View>(R.id.btn_carCtrl).setOnClickListener {
            gotoCarCtrl()
        }

        findViewById<View>(R.id.btn_header).setOnClickListener {
            gotoHeader()
        }

        findViewById<View>(R.id.btn_span).setOnClickListener {
            gotoSpan()
        }

        findViewById<View>(R.id.btn_lottie).setOnClickListener {
            gotoLottieActivity()
        }


        findViewById<View>(R.id.btn_car).setOnClickListener {
            gotoCarctivity()
        }

        findViewById<View>(R.id.btn_float_list).setOnClickListener {
            gotoFloatListActivity()
        }

        findViewById<View>(R.id.btn_video).setOnClickListener {
            gotoVideoActivity()
        }

        findViewById<View>(R.id.btn_fragment).setOnClickListener {
            gotoFragment()
        }

        findViewById<View>(R.id.btn_bottomSheet).setOnClickListener {
            gotoSheet()
        }

        findViewById<View>(R.id.btn_lifecycle).setOnClickListener {
            gotoLifecycle()
        }

        findViewById<View>(R.id.btn_sheetBehavior).setOnClickListener {
            gotoSheetBehavior()
        }

        findViewById<View>(R.id.btn_flow).setOnClickListener {
            gotoFlow()
        }
        findViewById<View>(R.id.btn_flow_avd).setOnClickListener {
            gotoFlowAvd()
        }
        findViewById<View>(R.id.btn_live_data).setOnClickListener {
            gotoLiveData()
        }

        // 蓝牙
        findViewById<View>(R.id.btn_bluetooth).setOnClickListener {
            //https://developer.android.com/reference/android/bluetooth/BluetoothDevice#createBond()
            helper.attemptRunCallback {
                gotoBluetooth()
            }
        }

        // 蓝牙LE
        findViewById<View>(R.id.btn_bluetooth_le).setOnClickListener {
            //attemptGotoBluetoothLePage()
            helper.attemptRunCallback {
                gotoBluetoothLe()
            }
        }

        findViewById<View>(R.id.btn_bluetooth_sample).setOnClickListener {
            //attemptGotoBluetoothLePage()
            helper.attemptRunCallback {
                gotoBluetoothSample()
            }
        }
    }

    // 显示用户隐私协议对话框
    private fun showPolicyAgree(simpleCallback: SimpleCallback) {
        val view: View = View.inflate(this, R.layout.alert_reg_help, null)
        val dialog = AlertDialog.Builder(this).setView(view).setCancelable(false).create()

        val textMessage = view.findViewById<TextView>(R.id.text_message)

        textMessage.setMovementMethod(LinkMovementMethod.getInstance())

        view.findViewById<View>(R.id.text_agree).setOnClickListener(View.OnClickListener {
            dialog.dismiss()
            simpleCallback.onAgree()
        })

        view.findViewById<View>(R.id.text_exit).setOnClickListener(View.OnClickListener() {
            dialog.dismiss()
            simpleCallback.onDisagree()
        })
        dialog.show()
        simpleCallback.onPrivacyShow()
    }

    private fun getActivity() = this

    private fun showAlertDialog(
        context: Context,
        messageResId: Int,
        leftCallback: (() -> Unit)? = null,
        rightCallback: (() -> Unit)? = null
    ) {
        CommonAskDialog.Builder(
            context,
            context.getString(R.string.charging_open_settings),
            context.getString(R.string.base_cancel),
            leftCallback = leftCallback
        ).create(
            context.getString(messageResId), listener = { rightCallback?.invoke() })
    }

    fun gotoAMap() {
        startActivity(Intent(this, AMapEnterActivity::class.java))
    }

    fun gotoHeader() {
        startActivity(Intent(this, HeaderActivity::class.java))
    }

    fun gotoSpan() {
        startActivity(Intent(this, SpanTextActivity::class.java))
    }


    fun gotoPager() {
        startActivity(Intent(this, PagerActivity::class.java))
    }

    fun gotoFragment() {
        startActivity(Intent(this, FragmentsActivity::class.java))
    }

    fun gotoLottieActivity() {
        startActivity(Intent(this, LottieActivity::class.java))
    }

    fun gotoCarctivity() {
        startActivity(Intent(this, CarActivity::class.java))
    }

    fun gotoFloatListActivity() {
        startActivity(Intent(this, FloatListActivity::class.java))
    }

    fun gotoKeyboardActivity() {
        startActivity(Intent(this, KeyboardActivity::class.java))
    }

    fun gotoRectActivity() {
        startActivity(Intent(this, RectActivity::class.java))
    }

    fun gotoRectAreaActivity() {
        startActivity(Intent(this, RectAreaActivity::class.java))
    }

    fun gotoVideoActivity() {
       // startActivity(Intent(this, VideoActivity::class.java))
    }

    fun gotoAnim() {
        startActivity(Intent(this, AnimActivity::class.java))
    }

    fun gotoCarCtrl() {
        startActivity(Intent(this, CarCtrlActivity::class.java))
    }

    fun gotoEventBus() {
        startActivity(Intent(this, EventbusCreateActivity::class.java))
    }

    fun gotoSheet() {
        startActivity(Intent(this, SheetActivity::class.java))
    }

    fun gotoLifecycle() {
        startActivity(Intent(this, LifecycleObserverActivity::class.java))
    }

    fun gotoSheetBehavior() {
        startActivity(Intent(this, SheetBehaviorActivity::class.java))
    }

    fun gotoLiveData() {
        startActivity(Intent(this, LiveDataActivity::class.java))
    }

    fun gotoFlow() {
        startActivity(Intent(this, FlowActivity::class.java))
    }

    fun gotoFlowAvd() {
        startActivity(Intent(this, FlowAvdActivity::class.java))
    }

    fun gotoBluetooth() {
        startActivity(Intent(this, BluetoothActivity::class.java))
    }

    fun gotoBluetoothLe() {
        startActivity(Intent(this, BleActivity::class.java))
    }

    fun gotoBluetoothSample() {
        startActivity(Intent(this, BluetoothSampleActivity::class.java))
    }

    private val helper = BluetoothPermissionHelper(this, object : BluetoothPermissionHelper.OnEnterSettingPage {
        override fun onEnterPositionSetting() {
            showAlertDialog(
                getActivity(),
                R.string.cs_permission_prompt_location,
                leftCallback = {
                    Toast.makeText(getActivity(), "已取消定位授权", Toast.LENGTH_LONG).show()
                },
                rightCallback = {
                    try {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    } catch (e: Exception) {
                        startActivity(Intent(Settings.ACTION_SETTINGS))
                    }
                })
        }

        override fun onEnterNearbySetting() {
            showAlertDialog(
                getActivity(),
                R.string.cs_permission_prompt_bluetooth,
                leftCallback = {
                    Toast.makeText(getActivity(), "已取消蓝牙授权", Toast.LENGTH_LONG).show()
                },
                rightCallback = {
                    try {
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", packageName, null)
                        ).let {
                            startActivity(it)
                        }
                    } catch (e: Exception) {
                        //ignore
                    }
                })
        }
    })

    override fun onDestroy() {
        super.onDestroy()
        viewModel.unReg(this)
    }

}