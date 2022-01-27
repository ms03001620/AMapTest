package com.example.amaptest

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.CustomMapStyleOptions
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.MyLocationStyle
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
    lateinit var stationsList : List<StationDetail>
    lateinit var mMapProxy: MapProxy
    lateinit var markerAction: MarkerAction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initData()
        setupMap(savedInstanceState)
        initCamera()



        val prev = JsonTestUtil.mock(stationsList.subList(0, 2), stationsList.subList(2, 4))
        val curr = JsonTestUtil.mock(
            listOf(
                stationsList.subList(0, 1).first(),
                stationsList.subList(2, 3).first()
            ),
            listOf(
                stationsList.subList(1, 2).first(),
                stationsList.subList(3, 4).first()
            ),
        )



/*
        val prev = JsonTestUtil.mock(stationsList.subList(0, 4), stationsList.subList(6, 8))

        val curr = JsonTestUtil.mock(
            stationsList.subList(0, 1),
            stationsList.subList(1, 2),
            stationsList.subList(2, 4),

            stationsList.subList(6, 8)
        )
*/

/*        val prev = JsonTestUtil.mock(stationsList.subList(0, 4))

        val curr = JsonTestUtil.mock(
            stationsList.subList(0, 1),
            stationsList.subList(1, 2),
            stationsList.subList(2, 4),
        )*/


        markerAction.setList(prev)
        findViewById<View>(R.id.btn_add).setOnClickListener {
            markerAction.setList(prev)
        }

        findViewById<View>(R.id.btn_move).setOnClickListener {
            ClusterUtils.createClusterAnimData(prev, curr, 1f).let {
                markerAction.processNodeList(it)
            }
        }


        findViewById<View>(R.id.btn_del).setOnClickListener {
            ClusterUtils.createClusterAnimData(curr, prev, 2f).let {
                markerAction.processNodeList(it)
            }
        }

        findViewById<View>(R.id.btn_center).setOnClickListener {
            markerAction.clear()
        }

        findViewById<View>(R.id.btn_fn).setOnClickListener {
            Toast.makeText(this, "size:${mMapProxy.getAllMarkers().size}", Toast.LENGTH_SHORT)
                .show()
            markerAction.printMarkers()
        }
    }

    val default = lazy {JsonTestUtil.mock(stationsList.subList(0, 1)).first()}
    val defaultCluster = lazy {JsonTestUtil.mock(stationsList.subList(5, 7)).first()}

    fun initData(){
        AssetsReadUtils.mockStation(this, "json_stations.json")?.let {
            stationsList = it
        }
        Log.d("MainActivity", "stations:$stationsList")
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
        val boundsBuilder = LatLngBounds.builder()
        for (station in stationsList) {
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