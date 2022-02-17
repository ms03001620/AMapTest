package com.example.amaptest

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.TextureMapView
import com.amap.api.maps.model.*
import com.example.amaptest.marker.*
import com.polestar.repository.data.charging.StationDetail
import com.polestar.repository.data.charging.toLatLng


class MapPerformanceActivity : AppCompatActivity() {
    private val viewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory()
        )[MapPerformanceViewModel::class.java]
    }
    private val styleData by lazy {
        AssetsReadUtils.readBytes(this, "style.data")
    }
    private val styleDataExtra by lazy {
        AssetsReadUtils.readBytes(this, "style_extra.data")
    }

    lateinit var mMapView: TextureMapView
    lateinit var stationsList : List<StationDetail>
    lateinit var mMapProxy: MapProxy
    lateinit var markerAction: MarkerAction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_performance)
        setupMap(savedInstanceState)
        initCamera()
        initObserver()

        findViewById<View>(R.id.btn_zoom_in)?.setOnClickListener {
            mMapView.map.animateCamera(CameraUpdateFactory.zoomIn())
        }

        findViewById<View>(R.id.btn_zoom_out)?.setOnClickListener {
            mMapView.map.animateCamera(CameraUpdateFactory.zoomOut())
            //mMapView.map.mapScreenMarkers.first().remove()
        }
    }

    private fun initObserver() {
        viewModel.dataLiveData.observe(this) {
            it?.let {
                markerAction.setList(it)
            }
        }
        viewModel.loadDefault(this, FILE, SUBLIST_START, SUBLIST_END)
    }

    private fun moveCameraToDataArea() {
        mMapView.map.moveCamera(
            // 12f -> 13f  cluster(1->2)
            CameraUpdateFactory.newLatLngZoom(LatLng(DEFAULT_LAT, DEFAULT_LNG), ZOOM)
        )
    }


    fun setupMap(savedInstanceState: Bundle?){
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
        mMapView.map.uiSettings.isMyLocationButtonEnabled = false
        mMapView.map.uiSettings.isScaleControlsEnabled = true
        mMapView.map.uiSettings.isZoomControlsEnabled = false
        mMapView.map.isMyLocationEnabled = false
        //mMapView.map.setOnMarkerClickListener(this)
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

    fun initCamera() {
        moveCameraToDataArea()
    }

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
        const val DEFAULT_LNG = 121.483231
        const val DEFAULT_LAT = 31.23128

        const val ZOOM = 16f

        const val FILE = "json_stations1.json"
        const val SUBLIST_START = -1 //-1 disable
        const val SUBLIST_END = -1 //-1 disable
    }

}