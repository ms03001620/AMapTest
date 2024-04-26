package com.polestar.customerservice.ui.drive.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.CustomMapStyleOptions
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.MarkerOptions
import com.blankj.utilcode.util.ConvertUtils
import com.example.amaptest.R
import com.example.amaptest.databinding.CsFragmentDriveMapBinding
import com.polestar.base.ext.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class DriveMapFragment : Fragment() {
    private lateinit var binding: CsFragmentDriveMapBinding

    private val mStyleData by lazy {
        ConvertUtils.inputStream2Bytes(requireActivity().assets.open("style.data"))
    }

    private val mStyleExtraData by lazy {
        ConvertUtils.inputStream2Bytes(requireActivity().assets.open("style_extra.data"))
    }

    private val iconStart by lazy {
        BitmapDescriptorFactory.fromResource(R.drawable.amap_start)
    }

    private val iconEnd by lazy {
        BitmapDescriptorFactory.fromResource(R.drawable.amap_end)
    }

    private lateinit var driverMoveHelper: DriverMoveHelper


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 合规检查，防止地图白屏
        MapsInitializer.updatePrivacyShow(context, true, true)
        MapsInitializer.updatePrivacyAgree(context, true)
        binding.mapView.onCreate(savedInstanceState)

        binding.mapView.apply {
            map.uiSettings.apply {
                // 样式配置，一般是在官网做配置然后下载下来
                map.setCustomMapStyle(
                    CustomMapStyleOptions().apply {
                        isEnable = true
                        styleData = mStyleData
                        styleExtraData = mStyleExtraData
                    }
                )
                // 定位按钮是否展示，比如划到地图的其他地方，只需要点这个按钮就能回到当前定位位置
                isMyLocationButtonEnabled = false
                // 比例尺是否可用，一般在底部
                isScaleControlsEnabled = false
                // 是否展示缩放按钮，一般在右边，一个 加号 和一个 减号
                isZoomControlsEnabled = false
                // 禁用所有的手势
                setAllGesturesEnabled(false)

                isRotateGesturesEnabled = true
                isZoomGesturesEnabled = true
                isGestureScaleByMapCenter = true
            }
        }
        binding.mapView.map.addOnMapLoadedListener(object: AMap.OnMapLoadedListener{
            override fun onMapLoaded() {
                //markHeight()
               // moveToVisible2()
            }
        })

        driverMoveHelper = DriverMoveHelper(binding.mapView.map, startLatLng, endLatLng)

        addMarkets()
        moveToVisible()
        mockDriverMove()
    }

    private fun mockDriverMove(){

        lifecycleScope.launch {
            delay(3000)

            driverTrack.forEach {
                driverMoveHelper.putDriver(it)
                delay(1000)
            }
        }

    }


    private fun addMarkets() {
        binding.mapView.map.let {
            it.addMarker(createMarket("start", startLatLng, iconStart))
            it.addMarker(createMarket("end", endLatLng, iconEnd))

            // 屏蔽marker点击事件
            it.setOnMarkerClickListener {
                true
            }
        }
    }


    private fun moveToVisible(padding: Int = 40.dp, callback: AMap.CancelableCallback?=null) {
        LatLngBounds.Builder().apply {
            this.include(startLatLng)
            this.include(endLatLng)
        }.build().let {
            CameraUpdateFactory.newLatLngBounds(it, padding)
        }.let {
            binding.mapView.map.animateCamera(it, callback)
        }
    }

    private fun moveToVisible2(padding: Int = 10.dp) {
        LatLngBounds.Builder().apply {
            this.include(startLatLng)
            this.include(endLatLng)
            this.include(markHeight())
        }.build().let {
            CameraUpdateFactory.newLatLngBounds(it, padding)
        }.let {
            binding.mapView.map.moveCamera(it)
        }
    }

    private fun markHeight(): LatLng {
        println(iconStart.height)
        val point= binding.mapView.map.projection.toScreenLocation(startLatLng)
        point.y -= iconStart.height
        return binding.mapView.map.projection.fromScreenLocation(point)
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        binding.mapView.onDestroy()
        super.onDestroyView()
    }


    // https://lbs.amap.com/api/android-sdk/guide/draw-on-map/draw-marker
    private fun createMarket(name: String, pos: LatLng, icon: BitmapDescriptor) = MarkerOptions()
        .position(pos)
        .icon(icon)
        .anchor(0.5f,0.5f)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CsFragmentDriveMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object{
        val startLatLng = LatLng(31.23646044, 121.48020424)

        val endLatLng = LatLng(31.14589905, 121.44282868)

        val endMark = LatLng(31.25, 121.48020424)

        val driverLatLng = LatLng(53.49173138, 122.34166052)

        val driverTrack = (1..18).map { i ->
            val lat = driverLatLng.latitude
            LatLng(lat - i, driverLatLng.longitude)
        }

        //DriverLocation(lat=53.49173138, lng=122.34166052, time=1695352015000)
    }
}