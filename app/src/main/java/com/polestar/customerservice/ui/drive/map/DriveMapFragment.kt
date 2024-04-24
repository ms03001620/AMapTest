package com.polestar.customerservice.ui.drive.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.CustomMapStyleOptions
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.MarkerOptions
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.SizeUtils
import com.example.amaptest.databinding.CsFragmentDriveMapBinding
import com.polestar.base.ext.dp

class DriveMapFragment : Fragment() {
    private lateinit var binding: CsFragmentDriveMapBinding

    private val mStyleData by lazy {
        ConvertUtils.inputStream2Bytes(requireActivity().assets.open("style.data"))
    }

    private val mStyleExtraData by lazy {
        ConvertUtils.inputStream2Bytes(requireActivity().assets.open("style_extra.data"))
    }

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
                setAllGesturesEnabled(true)
            }

            moveToVisible(map)
        }

        addMarkets()
    }

    private fun addMarkets() {
        binding.mapView.map.let {
            it.addMarker(createMarket("start", startLatlLng))
            it.addMarker(createMarket("end", endLatlLng))
        }
    }

    // https://lbs.amap.com/api/android-sdk/guide/draw-on-map/draw-marker
    private fun createMarket(name: String, pos: LatLng) = MarkerOptions()
        .position(pos)
        .title(name)
        .snippet("内容这里")

    private fun moveToVisible(map: AMap, padding: Int = 20.dp) {
        LatLngBounds.Builder().apply {
            this.include(startLatlLng)
            this.include(endLatlLng)
        }.build().let {
            CameraUpdateFactory.newLatLngBounds(it, padding)
        }.let {
            map.animateCamera(it)
        }
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CsFragmentDriveMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object{
        val startLatlLng = LatLng(31.23646044, 121.48020424)

        val endLatlLng = LatLng(31.14589905, 121.44282868)
    }
}