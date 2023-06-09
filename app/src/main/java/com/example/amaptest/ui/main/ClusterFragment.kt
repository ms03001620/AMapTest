package com.example.amaptest.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.*
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.CustomMapStyleOptions
import com.example.amaptest.AssetsReadUtils
import com.example.amaptest.R
import com.example.amaptest.ViewModelFactory
import com.example.amaptest.marker.MarkerActionActivity
import com.polestar.charging.ui.cluster.base.ClusterItem
import com.polestar.charging.ui.cluster.base.DistanceInfo
import com.polestar.charging.ui.cluster.view.DefaultClusterRenderer

class ClusterFragment : Fragment(),
    LocationSource {

    private val clusterViewModel by lazy {
        ViewModelProvider(
            requireActivity(),
            ViewModelFactory()
        )[ClusterViewModel::class.java]
    }

    private val styleData by lazy {
        AssetsReadUtils.readBytes(requireActivity(), "style_v780.data")
    }
    private val styleDataExtra by lazy {
        AssetsReadUtils.readBytes(requireActivity(), "style_extra_v780.data")
    }

    private val clusterIconSize by lazy {
        resources.getDimension(R.dimen.charging_station_cluster_size)
    }

    private lateinit var mapView: MapView
    private var locationClient: AMapLocationClient? = null
    private lateinit var mRenderer: DefaultClusterRenderer<ClusterItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.charging_layout_map, container, false)
        initMap(view, savedInstanceState)
        initClusterRenderer()
        initMapEvent()
        initZoomBtn(view)
        return view
    }

    private fun initMap(view: View, savedInstanceState: Bundle?) {

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
    }

    private fun initClusterRenderer() {
        mRenderer = DefaultClusterRenderer(requireContext(), mapView.map)
    }

    private fun initMapEvent() {
        clusterViewModel.clusterLiveData.observe(viewLifecycleOwner) { set ->
            mRenderer.onClustersChanged(set)
        }

        mapView.map.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChange(p0: CameraPosition?) {
            }

            override fun onCameraChangeFinish(p0: CameraPosition?) {
                clusterViewModel.createCluster(getClusterMergeDistance())
            }
        })

        mapView.map.setOnMapLoadedListener {
            val fileName = arguments?.getString("file_name") ?: FILE
            clusterViewModel.mock(requireContext(), getClusterMergeDistance(), fileName)
            moveCameraToDefault()
        }
    }

    private fun moveCameraToDefault() {
        with(MockUtils.mockBaiYulan()) {
            CameraUpdateFactory.newLatLngZoom(this, ZOOM)
        }.let {
            mapView.map.moveCamera(it)
        }
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

    override fun activate(p0: LocationSource.OnLocationChangedListener?) {
        locationClient = AMapLocationClient(requireContext())
        val locationOptions = AMapLocationClientOption()
        locationOptions.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
        locationOptions.isOnceLocation = true
        locationClient?.setLocationOption(locationOptions)
        locationClient?.startLocation()
    }

    override fun deactivate() {
        locationClient?.stopLocation()
        locationClient?.onDestroy()
        locationClient = null
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

    companion object {
        fun newInstance() = ClusterFragment()
        const val ZOOM = 14f
        const val FILE = "json_stations570.json"
    }
}