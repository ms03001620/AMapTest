package com.example.amaptest.ui.main

import android.content.Context
import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.*
import com.amap.api.maps.model.*
import com.example.amaptest.AssetsReadUtils
import com.example.amaptest.R
import com.example.amaptest.SizeUtils
import com.example.amaptest.ViewModelFactory
import com.polestar.charging.ui.cluster.base.StationClusterItem
import com.polestar.repository.data.charging.StationDetail
import com.polestar.repository.data.charging.freeAcDcAll
import com.polestar.repository.data.charging.isValid
import com.polestar.charging.ui.cluster.base.Cluster
import com.polestar.charging.ui.cluster.base.ClusterItem
import com.polestar.charging.ui.cluster.base.DistanceInfo
import com.polestar.charging.ui.cluster.view.DefaultClusterRenderer
import java.lang.StringBuilder

class ClusterFragment : Fragment(),
    LocationSource,
    AMapLocationListener,
    AMap.OnMarkerClickListener {
    companion object {
        fun newInstance() = ClusterFragment()
    }

    private val viewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            ViewModelFactory()
        )[MainViewModel::class.java]
    }

    private val clusterViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            ViewModelFactory()
        )[ClusterViewModel::class.java]
    }

    lateinit var mapView: MapView
    private val styleData by lazy {
        AssetsReadUtils.readBytes(requireActivity(), "style_v780.data")
    }
    private val styleDataExtra by lazy {
        AssetsReadUtils.readBytes(requireActivity(), "style_extra_v780.data")
    }

    private val clusterIconSize by lazy {
        resources.getDimension(R.dimen.charging_station_cluster_size)
    }

    private lateinit var myLocationStyle: MyLocationStyle
    private var locationClient: AMapLocationClient? = null
    private var locationOptions: AMapLocationClientOption? = null
    private var onLocationListener: LocationSource.OnLocationChangedListener? = null
    private var currentShownMarker: Marker? = null
    private val markersMap = HashMap<Marker, StationDetail>()


    lateinit var mRenderer: DefaultClusterRenderer<ClusterItem>
    fun initClusterRenderer(context: Context, map: AMap){
        mRenderer = DefaultClusterRenderer(context, map)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.charging_layout_map, container, false)
        MapsInitializer.updatePrivacyShow(requireContext(), true, true)
        MapsInitializer.updatePrivacyAgree(requireContext(), true)
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
        initClusterRenderer(requireContext(), mapView.map)
        initObserver()
        initClusterObserver()
        initZoomBtn(view)

        mapView.map.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChange(p0: CameraPosition?) {
            }

            override fun onCameraChangeFinish(p0: CameraPosition?) {
                clusterViewModel.reCalcCluster(getClusterMergeDistance())
            }
        })

        clusterViewModel.initClusterAlgorithm(clusterIconSize)

        mapView.map.setOnMapLoadedListener {
            clusterViewModel.mock(requireContext(), getClusterMergeDistance())
            //viewModel.mock(requireContext())
            moveCameraToDefault()
        }
        return view
    }

    private fun initClusterObserver() {
        clusterViewModel.stationClusterLiveData.observe(viewLifecycleOwner) { set ->
            mRenderer.onClustersChanged(set)

/*
mapView.map.clear()
set.forEach { cluster ->
                addMarkToMap(
                    cluster,
                    mapView.map
                )
            }*/
        }
    }

    private fun moveCameraToDefault() {
        with(MockUtils.mockBaiYulan()) {
            CameraUpdateFactory.newLatLng(this)
        }.let {
            mapView.map.moveCamera(it)
        }
    }


    private fun initObserver() {
        viewModel.stationListLiveData.observe(viewLifecycleOwner) {
            mapView.map.clear()
            markersMap.clear()
            currentShownMarker = null
            val boundsBuilder = LatLngBounds.builder()
            for (station in it) {
                getLatLng(station)?.let { latlng ->
                    addMarkToMap(latlng, station, mapView.map)?.let { marker ->
                        markersMap[marker] = station
                        boundsBuilder.include(latlng)
                    }
                }
            }
            mapView.map.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    boundsBuilder.build(),
                    100
                )
            )
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

        viewModel.stationLiveData.observe(viewLifecycleOwner) { station ->
            mapView.map.clear()
            markersMap.clear()
            currentShownMarker = null
            getLatLng(station)?.let { latlng ->
                addMarkToMap(latlng, station, mapView.map)?.let {
                    markersMap[it] = station
                    onMarkerClick(it)
                }
            }
        }
    }


    private fun addMarkToMap(
        latLng: LatLng,
        stationDetail: StationDetail,
        map: AMap,
        isCluster: Boolean = false,
    ): Marker? {
        return with(isCluster) {
            if (this) {
                getClusterBitmapDescriptor(0)
            } else {
                getCollapsedBitmapDescriptor(stationDetail)
            }
        }.let {
            MarkerOptions()
                .position(latLng)
                .icon(it)
                .infoWindowEnable(true)
        }.let {
            map.addMarker(it)
        }
    }

    private fun getLatLng(stationDetail: StationDetail): LatLng? {
        if (stationDetail.isValid() &&
            stationDetail.lat != null &&
            stationDetail.lng != null
        ) {
            return LatLng(stationDetail.lat, stationDetail.lng)
        }
        return null
    }

    fun getClusterMergeDistance() =
        DistanceInfo(
            clusterIconSize * mapView.map.scalePerPixel,
            mapView.map.cameraPosition.zoom != mapView.map.maxZoomLevel,
            mapView.map.cameraPosition?.zoom ?: 0f
        )

    private fun initZoomBtn(view: View) {
        view.findViewById<View>(R.id.btn_zoom_in)?.setOnClickListener {
            mapView.map.animateCamera(CameraUpdateFactory.zoomIn())
        }

        view.findViewById<View>(R.id.btn_zoom_out)?.setOnClickListener {
            mapView.map.animateCamera(CameraUpdateFactory.zoomOut())
        }
    }

    private fun getCollapsedBitmapDescriptor(stationDetail: StationDetail): BitmapDescriptor? {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.charging_layout_marker_collapsed, null, false)
        view.findViewById<TextView>(R.id.tv).text = stationDetail.freeAcDcAll().toString()
        return BitmapDescriptorFactory.fromView(view)
    }

    private fun getClusterBitmapDescriptor(clusterSize: Int): BitmapDescriptor? {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.charging_layout_marker_cluster, null, false)
        view.findViewById<TextView>(R.id.text_cluster).text = clusterSize.toString()
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
            p0?.setMarkerOptions(
                p0.options.icon(
                    getExpandedBitmapDescriptor(
                        currentStation?.freeDcTotal
                            ?: 0, currentStation?.freeAcTotal ?: 0
                    )
                )
            )

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