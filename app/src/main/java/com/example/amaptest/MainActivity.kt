package com.example.amaptest

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.CustomMapStyleOptions
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.animation.Animation
import com.example.amaptest.marker.*
import com.polestar.repository.data.charging.StationDetail
import com.polestar.repository.data.charging.toLatLng


class MainActivity : AppCompatActivity() {
    private val styleData by lazy {
        AssetsReadUtils.readBytes(this, "style.data")
    }
    private val styleDataExtra by lazy {
        AssetsReadUtils.readBytes(this, "style_extra.data")
    }

    lateinit var mMapView: MapView
    lateinit var stations : List<StationDetail>
    lateinit var mMapProxy: MapProxy
    lateinit var markerAction: MarkerAction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initData()
        setupMap(savedInstanceState)
        initCamera()

        findViewById<View>(R.id.btn_add).setOnClickListener {
            mMapProxy.createMarkers(default.value)
        }

        findViewById<View>(R.id.btn_move).setOnClickListener {
            val marker = mMapProxy.getMarker(default.value)!!
            mMapProxy.updateMarker(marker, defaultCluster.value)
        }

        findViewById<View>(R.id.btn_del).setOnClickListener {
            markerAction.attemptTransfer(default.value, defaultCluster.value.getLatlng()!!,
                listener = object: Animation.AnimationListener{
                    override fun onAnimationStart() {
                        logd("onAnimationStart", "MainActivity")
                    }

                    override fun onAnimationEnd() {
                        logd("onAnimationEnd", "MainActivity")
                    }
                })
        }

        findViewById<View>(R.id.btn_center).setOnClickListener {
            markerAction.attemptTransfer(default.value, defaultCluster.value.getLatlng()!!,
                listener = object: Animation.AnimationListener{
                    override fun onAnimationStart() {
                        logd("onAnimationStart", "MainActivity")
                    }

                    override fun onAnimationEnd() {
                        logd("onAnimationEnd", "MainActivity")
                    }
                }
                , removeAtEnd = true)
        }

        findViewById<View>(R.id.btn_offset).setOnClickListener {
            val data = JsonTestUtil.mock(stations.subList(7, 15)).first()

            markerAction.attemptTransfer(data, moveTo = default.value.getLatlng()!!, removeAtEnd = true)
        }
    }

    val default = lazy {JsonTestUtil.mock(stations.subList(0, 1)).first()}
    val defaultCluster = lazy {JsonTestUtil.mock(stations.subList(5, 7)).first()}

    fun initData(){
        AssetsReadUtils.mockStation(this, "json_stations.json")?.let {
            stations = it
        }
        Log.d("MainActivity", "stations:$stations")
    }

    fun setupMap(savedInstanceState: Bundle?){
        mMapView = findViewById(R.id.map)
        mMapView.onCreate(savedInstanceState)
        mMapProxy = MapProxy(mMapView.map, applicationContext)
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
        val boundsBuilder = LatLngBounds.builder()
        for (station in stations) {
            boundsBuilder.include(station.toLatLng())
        }
        mMapView.map.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                boundsBuilder.build(),
                100
            )
        )
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
}