package com.sp.app.maplib.ui.map

import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.sp.app.maplib.Constant
import com.sp.app.maplib.MapController
import com.sp.app.maplib.OnMapControllerListener
import com.sp.app.maplib.data.MapLocation
import com.sp.app.maplib.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel


abstract class MapBaseFragment : BaseFragment(), OnMapReadyCallback {

    val viewModel: MapViewModel by viewModel()

    // ---------------------------------------------------------------------------------------------

    private val mapControllerListener = object : OnMapControllerListener {
        override fun onStatus(status: MapController.Companion.MAP_STATUS) {
            viewModel.mapRepository.mapStatusString.postValue("" + status)
        }

        override fun onRequestCurrent() {
            val location = viewModel.getCurrentLocation() ?: return
            viewModel.mapCtl?.onCurrent(location)
        }

        override fun onChangeAngle(angle: Float) {
            viewModel.onChangeAngle(angle)
        }

        override fun onChangeZoom(zoom: Float) {
            viewModel.onChangeZoom(zoom)
        }

        override fun onLastCurrent(latLng: LatLng, point: Point) {
            onMapToCurrentPosition(latLng, point)
        }

        override fun onLongClickMap(point: MapLocation) {
            viewModel.onLongClickMap(point)
        }

        override fun onPoiClickMap(point: MapLocation) {}
        override fun onMoveStartMap(isTouch: Boolean) {}
        override fun onMoveMap() {}
        override fun onMoveEndMap(isTouch: Boolean) {}
        override fun onMarkerClickMap(
            marker_type: MapController.Companion.MARKER_TYPE,
            marker_id: String
        ) {
        }
    }

    //
    open fun onCreateViewBindingSetup(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) {
        // マップのサイズをViewのサイズに調整する処理
        viewSizeSetup()

        setMapSize()

        setAngleToViewSize(viewModel.angleMode.value!!)

        /////////////////////////////////////
        viewModel.initialize()

        /////////////////////////////////////
        // MapFragmentの生成と設定
        val mapId = getMapResourceId()
        if (mapId != -1) {
            var map: SupportMapFragment =
                childFragmentManager.findFragmentById(mapId) as SupportMapFragment
            if (map == null) {
                map = SupportMapFragment.newInstance()
                childFragmentManager.beginTransaction().replace(mapId, map).commit()
            }
            map.getMapAsync(this)
        }
    }

    //
    override fun onActivityCreate(savedInstanceState: Bundle?) {
        viewModel.create()
    }

    override fun onResume() {
        super.onResume()
        viewModel.resume()
        setObserve()
    }

    override fun onPause() {
        super.onPause()
        viewModel.pause()
        removeObserve()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.destroy()
    }

    //
    override fun onBack() {}

    ////////////////////////////////////////////////////////////////////////
    private fun setObserve() {

        viewModel.currentDirection.observe(this) {}
        viewModel.deviceOrientation.observe(this) {
            moveAngle(it)
            onChangeDeviceOrientation(it)
        }

        viewModel.angleMode.observe(this) { angleMode ->
            setAngleToViewSize(angleMode)
        }

    }

    private fun removeObserve() {
        viewModel.currentLocation.removeObservers(this)
        viewModel.deviceOrientation.removeObservers(this)
        viewModel.angleMode.removeObservers(this)
    }

    ////////////////////////////////////////////////////////////////////////

    // マップから取得した現在位置
    open fun onMapToCurrentPosition(latLng: LatLng, point: Point) {}

    // デバイスの方向が更新された時呼び出される
    open fun onChangeDeviceOrientation(orientation: Float) {}

    //
    open fun onChangeAngleMode(angleMode: Int) {}

    // マップのViewのサイズ
    open fun onMapArea(startPoint: Point, endPoint: Point) {}

    open fun onChangeAngle(
        angleMode: Int,
        deviceOrientation: Float,
        tilt: Float
    ) {
    }

    open fun getMainFrame(): View? {
        return null
    }

    open fun getMapFrame(): View? {
        return null
    }

    open fun getMapResourceId(): Int {
        return -1
    }

    ////////////////////////////////////////////////////////////////////////
    private fun setMapSize() {
        getMapFrame()?.let { view ->
            val data = Constant.getDisplaySize(requireActivity())
            view.layoutParams.height = (data.y * 1.4f).toInt()
        }
    }

    /**
     * マップの位置調整
     */
    private fun setAngleToViewSize(angleMode: Int) {

        val data = Constant.getDisplaySize(requireActivity())
        val y = if (angleMode != 0) {
            0f
        } else {
            -((data.y * 0.4f) / 2)
        }

        getMapFrame()?.let { view ->
            view.y = y
        }

        onChangeAngleMode(angleMode)
    }

    private fun viewSizeSetup() {

        getMainFrame()?.let { mainView ->
            mainView.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    mainView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    //Log.i(TAG, "addOnGlobalLayoutListener")
                    // 地図を表示するViewのサイズを一回だけ取得
                    val width = mainView.width
                    val height = mainView.height

                    viewModel.setViewSize(width, height)
                }
            })
        }
    }

    // MapFragmentの初期化関連
    private fun setMapViewSize(width: Int, height: Int) {

        val startPoint = Point(0, height / 10)
        val endPoint = Point(width, height - (height / 5 * 2))

        onMapArea(startPoint, endPoint)

        val areaWidth = endPoint.x - startPoint.x
        val areaHeight = endPoint.y - startPoint.y

        viewModel.mapCtl?.onDisplayArea(areaWidth, areaHeight / 3 * 2)

        val centerMoveX = 0.0
        val centerMoveY = (areaHeight / 2 + startPoint.y / 2).toDouble()

        //centerMoveY = 700.0

        viewModel.mapCtl?.setMoveCenter(centerMoveX, centerMoveY)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        context?.let {
            viewModel.mapCtl = MapController(it, googleMap, mapControllerListener)

            val width = viewModel.getMapWidth()
            val height = viewModel.getMapHeight()

            setMapViewSize(width, height)
        }
    }

    ////////////////////////////////////////////////////////////////////////
    private fun moveAngle(deviceOrientation: Float) {
        val angleMode = viewModel.angleMode.value ?: return

        // 角度の判定
        val angle = deviceOrientation

        if (angleMode == 0) {
            onChangeAngle(angleMode, angle, 0f)
            return
        }
        if (angleMode == 2) {
            onChangeAngle(angleMode, 0f, 45f)
        } else {
            onChangeAngle(angleMode, 0f, 0f)
        }

        if (viewModel.view_mapStatus.value == MapController.Companion.MAP_STATUS.TOUCH_MOVE.toString()
            || viewModel.view_mapStatus.value == MapController.Companion.MAP_STATUS.MAP_DIRECT_MOVE.toString()
        ) {
            viewModel.mapCtl?.onAngle(angle)
            return
        }

        val location = viewModel.getCurrentLocation() ?: return
        viewModel.mapCtl?.onAngle(location, angle, false)
    }
}