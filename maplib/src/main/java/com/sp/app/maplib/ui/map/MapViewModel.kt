package com.sp.app.maplib.ui.map

import android.content.Context
import android.graphics.Point
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.sp.app.maplib.Constant
import com.sp.app.maplib.MapController
import com.sp.app.maplib.MapInterface
import com.sp.app.maplib.R
import com.sp.app.maplib.data.MapLocation
import com.sp.app.maplib.debug.view.DebugLineView
import com.sp.app.maplib.repository.MapRepository
import com.sp.app.maplib.util.MagneticSensor
import com.sp.app.maplib.util.MyDate


class MapViewModel(
    val context: Context,
    val mapRepository: MapRepository,
) : ViewModel() {

    companion object {
        const val TAG = "MapViewModel"
    }

    var firstUpdateMap = false

    ///////////////////////////////////////////////////
    val debug_log: MutableLiveData<String> = MutableLiveData<String>("")


    ///////////////////////////////////////////////////
    var view_zoom: LiveData<Float> = mapRepository.mapZoom
    var view_angle: LiveData<Float> = mapRepository.angle
    var view_mapStatus: LiveData<String> = mapRepository.mapStatusString

    var angleMode: LiveData<Int> = mapRepository.angleMode

    ///////////////////////////////////////////////////
    // 位置情報
    var currentLocation: LiveData<Location?> = mapRepository.currentLocation

    // 位置情報2点を結ぶ方角
    var currentDirection: LiveData<Float> = mapRepository.currentDirection

    // センサーから取得した方位
    var deviceOrientation: LiveData<Float> = mapRepository.deviceOrientation
    var magneticDirection: LiveData<Float> = mapRepository.magneticDirection


    ///////////////////////////////////////////////////
    var overlayViewPoint: MutableLiveData<Point?> = MutableLiveData<Point?>()

    ///////////////////////////////////////////////////
    fun initialize() {
        mapRepository.isMapLocationEnable = true
        firstUpdateMap = true

        mapRepository.startLocation()
    }

    fun create() {
        mapRepository.activityLifecycle.value = "create"

    }

    fun resume() {
        mapRepository.isMapLocationEnable = true
        mapRepository.activityLifecycle.value = "resume"


        // 現在位置が更新されたら取得する
        mapRepository.currentLocation.observeForever { location ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                setCurrentLocation(latLng)

                debug_log.postValue(getDebugText())
            }
        }
    }

    fun pause() {
        mapRepository.isMapLocationEnable = false
        mapRepository.activityLifecycle.value = "pause"
        mapRepository.currentLocation.removeObserver {}
    }

    fun destroy() {
        mapRepository.isMapLocationEnable
        mapRepository.stopLocation()
        mapCtl?.onClear()
        mapRepository.activityLifecycle.value = "destroy"
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // マップ制御系
    ////////////////////////////////////////////////////////////////////////////////////////////////
    var mapCtl: MapInterface? = null

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 地図の長押しで呼ばれる
     */
    fun setSelectLocation(pointData: MapLocation) {
        val address =
            Constant.getAddress(context, pointData.point.latitude, pointData.point.longitude)
        pointData.address = address

        Log.i(TAG, "setSelectLocation()")
        mapRepository.selectPoint(pointData)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private fun setCurrentLocation(latLng: LatLng) {
        mapCtl?.let {
            it.onCurrent(latLng)

            // 緯度経度の情報から表示する地図の現在値のXYの座標位置を取得する
            val point = it.onLatLogToScreenPosition(latLng)
            overlayViewPoint.postValue(point)

            // 足跡
            val markerOptions = Constant.getMarkerOptions(
                context,
                latLng,
                null,
                R.drawable.marker_log
            )
            markerOptions.anchor(0.5f, 0.5f)
            it.onCurrentLog(markerOptions)
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    fun buttonClickZoomIn() {


    }

    fun buttonClickZoomOut() {


    }

    fun buttonClickThisPosi() {
        if (mapRepository.getLocationCurrent() == null) return
        mapCtl?.onFocusCurrent(mapRepository.getLocationCurrent()!!)
    }

    fun buttonClickCompass() {
        if (mapRepository.getLocationCurrent() == null) return
        mapCtl?.onAngle(mapRepository.getLocationCurrent()!!, 0f)

        //zoom = 17.5f
        //angle = 0f

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    // --------------------------------------------------------

    fun onLongClickMap(pointData: MapLocation) {
        val address =
            Constant.getAddress(context, pointData.point.latitude, pointData.point.longitude)
        pointData.address = address

        Log.i(TAG, "setSelectLocation()")
        mapRepository.selectPoint(pointData)
    }

    fun onMarkerClickMap(
        marker_type: MapController.Companion.MARKER_TYPE,
        marker_id: String
    ) {

        if (marker_type == MapController.Companion.MARKER_TYPE.SELECT) {
            Log.v(TAG, "選択位置を選択")
            mapRepository.onClickSelectPoint(1, marker_id)
        } else if (marker_type == MapController.Companion.MARKER_TYPE.FAVORITE) {
            Log.v(TAG, "選択位置を選択")
            mapRepository.onClickSelectPoint(2, marker_id)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Fragmentからの制御
    ////////////////////////////////////////////////////////////////////////////////////////////////
    fun onChangeAngle(angle: Float) {
        mapRepository.compassAngle = angle
        mapRepository.angle.postValue(-(angle))
    }


    fun onChangeZoom(zoom: Float) {
        mapRepository.mapZoom.postValue(zoom)
    }

    fun changeCompassMode() {
        val angleMode = angleMode.value ?: return

        val newAngleMode = if (angleMode == 0) {
            1
        } else if (angleMode == 1) {
            2
        } else {
            0
        }

        setCompassMode(newAngleMode)

        //zoom = 17.5f
        //angle = 0f
    }

    fun setCompassMode(angleMode: Int) {
        mapRepository.angleMode.value = angleMode

        if (angleMode == 1) {
            mapCtl?.onTilt(0f)
        } else if (angleMode == 2) {
            mapCtl?.onTilt(90f)
        } else {
            val location = getCurrentLocation() ?: return
            mapCtl?.onAngle(location, 0f, false)
            mapCtl?.onTilt(0f)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    fun setViewSize(width: Int, height: Int) {
        mapRepository.windowWidth = width
        mapRepository.windowHeight = height
    }

    fun getMapWidth(): Int {
        return mapRepository.windowWidth
    }

    fun getMapHeight(): Int {
        return mapRepository.windowHeight
    }

    fun getCurrentLocation(): LatLng? {
        return mapRepository.getLocationCurrent()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    fun getDebugText(): String {
        var text = ""

        currentLocation.value?.let {
            text += "" + "緯度:${it.latitude} / 経度:${it.longitude}"
            text += "\n" + "速度:" + it.speed + " / 取得時間:" + MyDate.TimeString(it.time)
        }

        deviceOrientation.value?.let {
            text += "\n" + MagneticSensor.toOrientationString(it) + " : 本体角度: ${it}°"
        }

        currentDirection.value?.let {
            text += "\n" + MagneticSensor.toOrientationString(it) + " : 位置角度: ${it}°"
        }

        view_mapStatus.value?.let {
            text += "\n" + "MAP_STATUS : " + it
        }

        angleMode.value?.let {
            text += "\n" + "angleMode : " + it
        }

        return text
    }
}