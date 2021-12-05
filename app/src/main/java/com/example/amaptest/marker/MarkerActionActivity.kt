package com.example.amaptest.marker

import android.content.res.Resources
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.TextView
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.amap.api.maps.model.animation.AlphaAnimation
import com.amap.api.maps.model.animation.Animation
import com.amap.api.maps.model.animation.AnimationSet
import com.amap.api.maps.model.animation.TranslateAnimation
import com.example.amaptest.AssetsReadUtils
import com.example.amaptest.R
import com.polestar.repository.data.charging.StationDetail

class MarkerActionActivity : AppCompatActivity() {
    private val styleData by lazy {
        AssetsReadUtils.readBytes(this, "style.data")
    }
    private val styleDataExtra by lazy {
        AssetsReadUtils.readBytes(this, "style_extra.data")
    }

    lateinit var mMapView: MapView
    lateinit var stations : List<StationDetail>
    var markerStations = mutableListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marker_action)
        initData()
        setupMap(savedInstanceState)
        loadMarks()
        initBtns()
    }

    fun initBtns(){
        findViewById<View>(R.id.btn_move).setOnClickListener {
            move(markerStations[0], markerStations[1])
        }

        findViewById<View>(R.id.btn_del).setOnClickListener {
            markerStations[1].remove()
        }
    }

    private fun move(source: Marker, target: Marker){
        val set = AnimationSet(true)
        set.addAnimation(TranslateAnimation(target.position).apply {
            this.setDuration(1000)
        })

        set.addAnimation(AlphaAnimation(1f, .3f).apply {
            this.setDuration(1000)
            this.setInterpolator(AccelerateInterpolator())
        })

        source.setAnimationListener(object: Animation.AnimationListener{
            override fun onAnimationStart() {
            }

            override fun onAnimationEnd() {
                source.remove()
            }
        })

        source.setAnimation(set)
        source.startAnimation()
    }



    fun initData(){
        AssetsReadUtils.mockStation(this, "json_stations.json")?.let {
            stations = it.subList(0, 2)
        }
        Log.d("MainActivity", "stations:$stations")
    }

    fun setupMap(savedInstanceState: Bundle?){
        mMapView = findViewById(R.id.map)
        mMapView.onCreate(savedInstanceState)
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

    fun loadMarks() {
        val boundsBuilder = LatLngBounds.builder()
        for (station in stations) {
            if (station.lat != null && station.lng != null && station.acTotal != null && station.dcTotal != null) {
                val markerOptions = MarkerOptions()
                    .position(LatLng(station.lat, station.lng))
                    .icon(getCollapsedBitmapDescriptor((station.acTotal + station.dcTotal).toString()))
                    .infoWindowEnable(true)
                val marker = mMapView.map.addMarker(markerOptions)
                markerStations.add(marker)
                //markersMap[marker] = station
                boundsBuilder.include(LatLng(station.lat, station.lng))
            }
        }
        mMapView.map.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                boundsBuilder.build(),
                100
            )
        )
    }


    private fun getCollapsedBitmapDescriptor(total: String): BitmapDescriptor? {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.charging_layout_marker_collapsed, null, false)
        view.findViewById<TextView>(R.id.tv).text = total
        return BitmapDescriptorFactory.fromView(view)
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