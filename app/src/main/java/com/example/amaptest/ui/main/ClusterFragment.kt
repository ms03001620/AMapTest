package com.example.amaptest.ui.main

import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.LocationSource
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.example.amaptest.AssetsReadUtils
import com.example.amaptest.R
import com.example.amaptest.SizeUtils
import com.example.amaptest.ViewModelFactory
import com.polestar.repository.data.charging.StationDetail
import com.polestar.repository.data.charging.freeAcDcAll
import com.polestar.repository.data.charging.isValid
import java.lang.StringBuilder

class ClusterFragment : Fragment(),
    LocationSource,
    AMapLocationListener,
    AMap.OnMarkerClickListener
{

    companion object {
        fun newInstance() = ClusterFragment()
    }

    private val viewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            ViewModelFactory()
        )[MainViewModel::class.java]
    }

    lateinit var mapView: MapView
    private val styleData by lazy {
        AssetsReadUtils.readBytes(requireActivity(), "style.data")
    }
    private val styleDataExtra by lazy {
        AssetsReadUtils.readBytes(requireActivity(), "style_extra.data")
    }
    private lateinit var myLocationStyle: MyLocationStyle
    private var locationClient: AMapLocationClient? = null
    private var locationOptions: AMapLocationClientOption? = null
    private var onLocationListener: LocationSource.OnLocationChangedListener? = null
    private var currentShownMarker: Marker? = null
    private val markersMap = HashMap<Marker, StationDetail>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.charging_layout_map, container, false)
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.map.setCustomMapStyle(
            CustomMapStyleOptions()
                .setEnable(true)
                .setStyleData(styleData)
                .setStyleExtraData(styleDataExtra)
        )
        mapView.map.setLocationSource(this)
        mapView.map.uiSettings.isMyLocationButtonEnabled = false
        mapView.map.uiSettings.isScaleControlsEnabled = true
        mapView.map.uiSettings.isZoomControlsEnabled = false
        mapView.map.isMyLocationEnabled = false
        mapView.map.setOnMapClickListener {
            viewModel.setMarkerCollapsed()
        }
        mapView.map.setOnMarkerClickListener(this)
        myLocationStyle = MyLocationStyle()
        myLocationStyle.myLocationIcon(
            BitmapDescriptorFactory.fromResource(R.drawable.charging_pic_my_location_3x)
        )
        myLocationStyle.strokeColor(Color.parseColor("#0D101820"))
        myLocationStyle.strokeWidth(2f)
        myLocationStyle.radiusFillColor(Color.parseColor("#0A137ED4"))
        initObserver()
        viewModel.mock(requireContext())
        return view
    }

    private fun initObserver() {
        viewModel.stationLiveData.observe(viewLifecycleOwner) {
            mapView.map.clear()
            markersMap.clear()
            currentShownMarker = null
            if (it.isValid() && it.lat != null && it.lng != null) {
                val markerOptions = MarkerOptions()
                    .position(LatLng(it.lat!!, it.lng!!))
                    .icon(getCollapsedBitmapDescriptor(it))
                    .infoWindowEnable(true)
                mapView.map.animateCamera(CameraUpdateFactory.newLatLng(LatLng(it.lat!!, it.lng!!)))
                val marker = mapView.map.addMarker(markerOptions)
                markersMap[marker] = it
                onMarkerClick(marker)
            } else {
                viewModel.startLocation()
            }
        }
        viewModel.stationListLiveData.observe(viewLifecycleOwner) {
            mapView.map.clear()
            markersMap.clear()
            currentShownMarker = null
            val boundsBuilder = LatLngBounds.builder()
            for (station in it) {
                if (station.lat != null && station.lng != null) {
                    val markerOptions = MarkerOptions()
                        .position(LatLng(station.lat!!, station.lng!!))
                        .icon(getCollapsedBitmapDescriptor(station))
                        .infoWindowEnable(true)
                    val marker = mapView.map.addMarker(markerOptions)
                    markersMap[marker] = station
                    boundsBuilder.include(LatLng(station.lat!!, station.lng!!))
                }
            }
            mapView.map.animateCamera(
                CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(),
                100))
        }
        viewModel.markerCollapsedLiveData.observe(viewLifecycleOwner) {
            markersMap[currentShownMarker]?.let {
                currentShownMarker?.setMarkerOptions(
                    currentShownMarker?.options?.icon(
                        getCollapsedBitmapDescriptor(it)
                    )
                )
            }
            currentShownMarker = null
        }
        viewModel.locationLiveData.observe(viewLifecycleOwner) {
            mapView.map.isMyLocationEnabled = true
            mapView.map.myLocationStyle = myLocationStyle
        }
    }

    private fun getCollapsedBitmapDescriptor(stationDetail: StationDetail): BitmapDescriptor? {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.charging_layout_marker_collapsed, null, false)
        view.findViewById<TextView>(R.id.tv).text = stationDetail.freeAcDcAll().toString()
        return BitmapDescriptorFactory.fromView(view)
    }

    private fun getClusterBitmapDescriptor(stationDetail: StationDetail): BitmapDescriptor? {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.charging_layout_marker_cluster, null, false)
        view.findViewById<TextView>(R.id.text_cluster).text =  stationDetail.freeAcDcAll().toString()
        return BitmapDescriptorFactory.fromView(view)
    }

    private fun getExpandedBitmapDescriptor(freeDcTotal: Int, freeAcTotal: Int): BitmapDescriptor? {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.charging_layout_marker_expanded, null, false)

        with(view.findViewById(R.id.tv) as TextView) {
            val s = StringBuilder()
            s.append(getString(R.string.free_dc_total, freeDcTotal))
            s.append("|")
                .append(getString(R.string.free_ac_total, freeAcTotal))
            this.text = s.toString()
        }
        return BitmapDescriptorFactory.fromView(view)
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        //定位点也是一个marker，注意它的特殊性
        if (markersMap.containsKey(p0)) {
            if (currentShownMarker != p0) {
                markersMap[currentShownMarker]?.let {
                    currentShownMarker?.setMarkerOptions(
                        currentShownMarker?.options?.icon(
                            getCollapsedBitmapDescriptor((it))
                        )
                    )
                }
            }
            currentShownMarker = p0
            val currentStation = markersMap[currentShownMarker]
            p0?.setMarkerOptions(p0.options.icon(getExpandedBitmapDescriptor(currentStation?.freeDcTotal
                ?: 0, currentStation?.freeAcTotal ?: 0)))

            currentShownMarker?.let {
                moveCameraWithOffset(it, mapView.map, SizeUtils.dp2px(150f))
            }
            viewModel.markerExpandedCallback?.let { it(currentStation) }
        }
        return true
    }

    private fun moveCameraWithOffset(source: Marker, map: AMap, offsetPx: Int) {
        with(source.position) {
            map.projection.toScreenLocation(this)
        }.apply {
            this.y += offsetPx
        }.let {
            map.projection.fromScreenLocation(it)
        }.let {
            CameraUpdateFactory.changeLatLng(it)
        }.let {
            map.animateCamera(it)
        }
    }

    override fun activate(p0: LocationSource.OnLocationChangedListener?) {
        onLocationListener = p0
        locationClient = AMapLocationClient(requireContext())
        locationClient?.setLocationListener(this)
        locationOptions = AMapLocationClientOption()
        locationOptions?.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        locationOptions?.isOnceLocation = true
        locationClient?.setLocationOption(locationOptions)
        locationClient?.startLocation()
    }

    override fun deactivate() {
        onLocationListener = null
        locationClient?.stopLocation()
        locationClient?.onDestroy()
        locationClient = null
    }

    override fun onLocationChanged(p0: AMapLocation?) {
        onLocationListener?.onLocationChanged(p0)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

}