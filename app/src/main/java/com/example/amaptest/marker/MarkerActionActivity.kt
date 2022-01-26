package com.example.amaptest.marker

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.example.amaptest.AssetsReadUtils
import com.example.amaptest.R
import com.example.amaptest.ViewModelFactory
import com.polestar.base.utils.logd
import com.polestar.charging.ui.cluster.base.DistanceInfo

class MarkerActionActivity : AppCompatActivity() {
    private val viewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory()
        )[MarkerActionViewModel::class.java]
    }
    private val styleData by lazy {
        AssetsReadUtils.readBytes(this, "style.data")
    }
    private val styleDataExtra by lazy {
        AssetsReadUtils.readBytes(this, "style_extra.data")
    }

    private val clusterIconSize by lazy {
        resources.getDimension(R.dimen.charging_station_cluster_size)
    }

    lateinit var mMapView: MapView
    lateinit var mMapProxy: MapProxy
    lateinit var markerAction: MarkerAction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marker_action)
        setupMap(savedInstanceState)
        initZoomBtn()
        moveCameraToDataArea()
        initObserver()
    }

    private fun initObserver() {
        //viewModel.loadDefault(this, FILE, SUBLIST_START, SUBLIST_END)
        viewModel.noChangeLiveData.observe(this) {
            markerAction.setList(it)
        }

        viewModel.clusterAnimDataLiveData.observe(this) {
            markerAction.processNodeList(it)
        }
    }

    fun setupMap(savedInstanceState: Bundle?) {
        mMapView = findViewById(R.id.map)
        mMapView.onCreate(savedInstanceState)
        mMapProxy = MapProxy(BaseMap(mMapView.map), applicationContext)
        markerAction = MarkerAction(mMapProxy)
        mMapView.map.setCustomMapStyle(
            CustomMapStyleOptions()
                .setEnable(true)
                .setStyleData(styleData)
                .setStyleExtraData(styleDataExtra)
        )
        mMapView.map.setLocationSource(null)
        mMapView.map.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChange(p0: CameraPosition?) {
            }

            override fun onCameraChangeFinish(p0: CameraPosition?) {
                // move camera -> calc cluster
                viewModel.calcClusters(getClusterMergeDistance())
            }
        })
        mMapView.map.uiSettings.isMyLocationButtonEnabled = false
        mMapView.map.uiSettings.isScaleControlsEnabled = true
        mMapView.map.uiSettings.isZoomControlsEnabled = false
        mMapView.map.isMyLocationEnabled = false
        val myLocationStyle = MyLocationStyle()
        myLocationStyle.myLocationIcon(
            BitmapDescriptorFactory.fromResource(R.drawable.charging_pic_my_location_3x)
        )
        myLocationStyle.strokeColor(Color.parseColor("#0D101820"))
        myLocationStyle.strokeWidth(2f)
        myLocationStyle.radiusFillColor(Color.parseColor("#0A137ED4"))

        mMapView.map.isMyLocationEnabled = true
        mMapView.map.myLocationStyle = myLocationStyle
    }

    private fun moveCameraToDataArea() {
        mMapView.map.moveCamera(
            // 12f -> 13f  cluster(1->2)
            CameraUpdateFactory.newLatLngZoom(LatLng(DEFAULT_LAT,DEFAULT_LNG), ZOOM)
        )
    }

    private fun initZoomBtn() {
        findViewById<View>(R.id.btn_zoom_in)?.setOnClickListener {
            mMapView.map.animateCamera(CameraUpdateFactory.zoomIn())
        }

        findViewById<View>(R.id.btn_zoom_out)?.setOnClickListener {
            mMapView.map.animateCamera(CameraUpdateFactory.zoomOut())
            //mMapView.map.mapScreenMarkers.first().remove()
        }

        findViewById<View>(R.id.btn_zoom_fn)?.setOnClickListener {
            //testAddIcon()
            testRemove()
        }

        findViewById<View>(R.id.btn_zoom_co)?.setOnClickListener {
            testAddIcon()
        }
    }

    fun testRemove() {
        markerAction.clearMarker(viewModel.prev)
    }

    fun testPaint() {
        viewModel.printPrev()
        Toast.makeText(this, "size:${mMapProxy.getAllMarkers().size}", Toast.LENGTH_SHORT)
            .show()
        markerAction.printMarkers()
    }

    fun testAddIcon() {
        val start = System.currentTimeMillis()
        markerAction.testSyncAnim()
        logd("transfer b pass:${System.currentTimeMillis() - start}", "_____")
    }

    fun getClusterMergeDistance() =
        DistanceInfo(
            clusterIconSize * mMapView.map.scalePerPixel,
            mMapView.map.cameraPosition.zoom != mMapView.map.maxZoomLevel,
            mMapView.map.cameraPosition
        )


    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mMapView.onSaveInstanceState(outState)
    }

    companion object {
        // 白玉兰位置
        const val DEFAULT_LNG = 121.497798
        const val DEFAULT_LAT = 31.249051

        const val ZOOM = 12f

/*        const val FILE = "json_stations570.json"
        const val SUBLIST_START = -1 //-1 disable
        const val SUBLIST_END = -1 //-1 disable*/


        const val FILE = "json_stations570.json"
        const val SUBLIST_START = -1 //-1 disable
        const val SUBLIST_END = -1 //-1 disable
    }

}