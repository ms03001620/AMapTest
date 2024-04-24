package com.polestar.customerservice.ui.drive.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.BitmapDescriptor
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.CustomMapStyleOptions
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.route.BusRouteResult
import com.amap.api.services.route.DriveRouteResult
import com.amap.api.services.route.RideRouteResult
import com.amap.api.services.route.RouteSearch
import com.amap.api.services.route.WalkRouteResult
import com.blankj.utilcode.util.ConvertUtils
import com.example.amaptest.R
import com.example.amaptest.databinding.CsFragmentDriveMapBinding
import com.polestar.base.ext.dp
import com.polestar.base.views.PolestarToast
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
        BitmapDescriptorFactory.fromResource(R.drawable.amap_bus)
    }

    private val iconEnd by lazy {
        BitmapDescriptorFactory.fromResource(R.drawable.amap_end)
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
        }
        moveToVisible()
        addMarkets()
        loadPath()
        lifecycleScope.launch {
            delay(6000)
            moveToVisible2()
        }
    }

    //https://lbs.amap.com/api/android-sdk/guide/route-plan/drive
    private fun loadPath(){
        val routeSearch = RouteSearch(requireContext())
        routeSearch.setRouteSearchListener(object: RouteSearch.OnRouteSearchListener{
            override fun onBusRouteSearched(p0: BusRouteResult?, p1: Int) {
            }

            override fun onDriveRouteSearched(driveRouteResult: DriveRouteResult?, errorCode: Int) {
                if (errorCode != 1000 || driveRouteResult == null || driveRouteResult.paths.isNullOrEmpty()) {
                    PolestarToast.showShortToast("查询失败 errorCode:$errorCode")
                    return
                }
                drawRoute(driveRouteResult)
            }

            override fun onWalkRouteSearched(p0: WalkRouteResult?, p1: Int) {
            }

            override fun onRideRouteSearched(p0: RideRouteResult?, p1: Int) {
            }
        })

        val fromAndTo = RouteSearch.FromAndTo(
            LatLonPoint(startLatlLng.latitude, startLatlLng.longitude),
            LatLonPoint(endLatlLng.latitude, endLatlLng.longitude)
        )

        val req = RouteSearch.DriveRouteQuery(fromAndTo, 0, null, null, "")
        routeSearch.calculateDriveRouteAsyn(req)
    }

    private fun drawRoute(driveRouteResult: DriveRouteResult) {
        val drivePath = driveRouteResult.paths[0]
        val drivingRouteOverlay = DrivingRouteOverlay(
            requireContext(), binding.mapView.map, drivePath,
            driveRouteResult.startPos,
            driveRouteResult.targetPos, null
        )
        drivingRouteOverlay.setNodeIconVisibility(false) //设置节点marker是否显示
        drivingRouteOverlay.setIsColorfulline(true) //是否用颜色展示交通拥堵情况，默认true
        drivingRouteOverlay.removeFromMap()
        drivingRouteOverlay.addToMap(false)
        //drivingRouteOverlay.zoomToSpan()
        val dis = drivePath.distance.toInt()
        val dur = drivePath.duration.toInt()
        val des = AMapUtil.getFriendlyTime(dur) + "(" + AMapUtil.getFriendlyLength(dis) + ")"
        val taxiCost = driveRouteResult.taxiCost.toInt()
    }

    private fun addMarkets() {
        binding.mapView.map.let {
            it.addMarker(createMarket("start", startLatlLng, iconStart))
            it.addMarker(createMarket("end", endLatlLng, iconEnd))
            // 屏蔽marker点击事件
            it.setOnMarkerClickListener {
                true
            }
        }
    }

    // https://lbs.amap.com/api/android-sdk/guide/draw-on-map/draw-marker
    private fun createMarket(name: String, pos: LatLng, icon: BitmapDescriptor) = MarkerOptions()
        .position(pos)
        .icon(icon)


    private fun moveToVisible(padding: Int = 10.dp) {
        LatLngBounds.Builder().apply {
            this.include(startLatlLng)
            this.include(endLatlLng)
           // this.include(markHeight())
        }.build().let {
            CameraUpdateFactory.newLatLngBounds(it, padding)
        }.let {
            binding.mapView.map.moveCamera(it)
        }
    }

    private fun moveToVisible2(padding: Int = 10.dp) {
        LatLngBounds.Builder().apply {
            this.include(startLatlLng)
            this.include(endLatlLng)
            this.include(markHeight())
        }.build().let {
            CameraUpdateFactory.newLatLngBounds(it, padding)
        }.let {
            binding.mapView.map.moveCamera(it)
        }
    }

    private fun markHeight(): LatLng {
        println(iconStart.height)
        val point= binding.mapView.map.projection.toScreenLocation(startLatlLng)
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

        val endMark = LatLng(31.25, 121.48020424)
    }
}