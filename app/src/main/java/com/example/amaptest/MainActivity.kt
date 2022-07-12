package com.example.amaptest

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.*
import com.example.amaptest.marker.*
import com.example.amaptest.ui.main.MockUtils
import com.polestar.base.utils.logd
import com.polestar.repository.data.charging.StationDetail
import com.polestar.repository.data.charging.toLatLng
import kotlin.math.abs


class MainActivity : AppCompatActivity() {
    private val styleData by lazy {
        AssetsReadUtils.readBytes(this, "style_v780.data")
    }
    private val styleDataExtra by lazy {
        AssetsReadUtils.readBytes(this, "style_extra_v780.data")
    }

    lateinit var mMapView: MapView
    lateinit var stationsList: List<StationDetail>
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

        markerAction.setList(prev)
        findViewById<View>(R.id.btn_add).setOnClickListener {
            markerAction.setList(prev)
        }

/*        findViewById<View>(R.id.btn_plus).setOnClickListener {
            ClusterUtils.createClusterAnimData(prev, curr, 1f).let {
                markerAction.processNodeList(it)
            }
        }


        findViewById<View>(R.id.btn_sub).setOnClickListener {
            ClusterUtils.createClusterAnimData(curr, prev, 2f).let {
                markerAction.processNodeList(it)
            }
        }*/


        findViewById<View>(R.id.btn_plus).setOnClickListener {
            ClusterUtils.createClusterNoAnimData(prev, curr).let {
                markerAction.processNodeList(it)
            }
        }


        findViewById<View>(R.id.btn_sub).setOnClickListener {
            ClusterUtils.createClusterNoAnimData(curr, prev).let {
                markerAction.processNodeList(it)
            }
        }

        findViewById<View>(R.id.btn_center).setOnClickListener {
            markerAction.clear()
        }

        findViewById<View>(R.id.btn_fn).setOnClickListener {
            drawVisibleRegion(-0.1)

            prev.forEach {
                val isInclude = getLatLngBounds(
                    mMapView.map.projection.visibleRegion.latLngBounds,
                    -0.1
                ).contains(it.getLatlng())
                logd("______ $it, contain:$isInclude")
            }
        }
    }

    fun drawVisibleRegion(percentOffset: Double = 1.0) {
        getLatLngBounds(
            mMapView.map.projection.visibleRegion.latLngBounds,
            percentOffset
        ).let {
            getVisibleLatLngList(it)?.let { list ->
                PolygonOptions().also {
                    it.addAll(list)
                    it.fillColor(Color.TRANSPARENT)
                    it.strokeColor(Color.RED).strokeWidth(15f)
                }
            }?.let { poly ->
                mMapView.map.addPolygon(poly)
            }
        }
    }

    fun getVisibleLatLngList(bounds: LatLngBounds?): MutableList<LatLng>? {
        return bounds?.let {
            return mutableListOf<LatLng>().also {
                it.add(bounds.southwest)
                it.add(LatLng(bounds.southwest.latitude, bounds.northeast.longitude))
                it.add(bounds.northeast)
                it.add(LatLng(bounds.northeast.latitude, bounds.southwest.longitude))
            }
        }
    }

    fun getLatLngBounds(bounds: LatLngBounds, percentOffset: Double = 1.0): LatLngBounds {
        var offsetW = 0.0

        if (percentOffset != 0.0) {
            val width =
                abs(bounds.southwest.longitude - bounds.northeast.longitude)
            offsetW = width * percentOffset
        }

        return LatLngBounds(
            LatLng(bounds.southwest.latitude - offsetW, bounds.southwest.longitude - offsetW),
            LatLng(bounds.northeast.latitude + offsetW, bounds.northeast.longitude + offsetW)
        )
    }

    fun drawRedRectRoundMap() {
        mMapView.map.projection.visibleRegion?.let { bounds ->
            mutableListOf<LatLng>().also {
                it.add(bounds.nearLeft)
                it.add(bounds.farLeft)
                it.add(bounds.farRight)
                it.add(bounds.nearRight)
            }
        }?.let { list ->
            PolygonOptions().also {
                it.addAll(list)
                it.fillColor(Color.TRANSPARENT)
                it.strokeColor(Color.RED).strokeWidth(15f)
            }
        }?.let { poly ->
            mMapView.map.addPolygon(poly)
        }
    }


    fun drawBaiyuLanCircle() {
        val latLng = MockUtils.mockBaiYulan()
        mMapView.map.addCircle(
            CircleOptions()
                .center(latLng)
                .radius(1000.0)
                .fillColor(Color.argb(1, 1, 1, 1))
                .strokeColor(0xff00000)
                .strokeWidth(15f)
        )
    }

    private fun createRectangle(
        center: LatLng,
        halfWidth: Double = 1.0,
        halfHeight: Double = 1.0
    ): List<LatLng> {
        val latLngs: MutableList<LatLng> = ArrayList()
        latLngs.add(
            LatLng(
                center.latitude - halfHeight,
                center.longitude - halfWidth
            )
        )//left bottom; nearLeft
        latLngs.add(
            LatLng(
                center.latitude - halfHeight,
                center.longitude + halfWidth
            )
        )//left top; farLeft
        latLngs.add(
            LatLng(
                center.latitude + halfHeight,
                center.longitude + halfWidth
            )
        )//right top; farRight
        latLngs.add(
            LatLng(
                center.latitude + halfHeight,
                center.longitude - halfWidth
            )
        )//right bottom; nearRight
        return latLngs
    }

    val default = lazy { JsonTestUtil.mock(stationsList.subList(0, 1)).first() }
    val defaultCluster = lazy { JsonTestUtil.mock(stationsList.subList(5, 7)).first() }

    fun initData() {
        AssetsReadUtils.mockStation(this, "json_stations.json")?.let {
            stationsList = it
        }
        Log.d("MainActivity", "stations:$stationsList")
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