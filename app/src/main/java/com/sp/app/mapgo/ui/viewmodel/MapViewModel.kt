package com.sp.app.mapgo.ui.viewmodel

import android.content.Context
import android.graphics.Point
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.*
import com.sp.app.maplib.data.MapLocation
import com.sp.app.maplib.debug.view.DebugLineView
import com.sp.app.maplib.Constant
import com.sp.app.maplib.MapController
import com.sp.app.maplib.MapInterface
import com.sp.app.maplib.R
import com.sp.app.maplib.repository.MapRepository
import com.sp.app.maplib.util.MagneticSensor
import com.sp.app.maplib.util.MyDate


class MapViewModel(
    val mapRepository: MapRepository,
) : ViewModel() {

    companion object {
        const val TAG = "MapViewModel"
    }

    var context: Context? = null

    var firstUpdateMap = false

    ///////////////////////////////////////////////////

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

    fun resume(context: Context) {
        this.context = context
        mapRepository.isMapLocationEnable = true
        mapRepository.activityLifecycle.value = "resume"


        // 現在位置が更新されたら取得する
        mapRepository.currentLocation.observeForever { location ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                setCurrentLocation(latLng)
            }
        }

//        guideRepository.guideMode.observe(fragment, object : Observer<String?> {
//            override fun onChanged(guideMode: String?) {
//                if (guideMode != null) {
//                    mapCtl?.onStartGuide()
//                } else {
//                    mapCtl?.onStopGuide()
//                }
//            }
//        })

//        mapRepository.twoPointLiveData.observe(fragment, object : Observer<TwoPoint?> {
//            override fun onChanged(twoPoint: TwoPoint?) {
//                twoPoint?.let {
//                    mapCtl?.onFocusCenter(twoPoint.neLatLng, twoPoint.wsLatLng)
//                }
//            }
//        })
//        //////////////////////////////////////////////////////////////////////////////
        // GPSのログ情報が更新されたらマーカーを描画
//        mapRepository.gpsLogLocation.observe(fragment, object : Observer<Location?> {
//            override fun onChanged(location: Location?) {
//                if (location != null) {
//                    //Log.v("!!!!!!!!!!","GPSログが更新されました")
//                    val latLng = LatLng(location.latitude, location.longitude)
//                    val markerOptions = getMarkerOptions(latLng, null, com.sp.app.maplib.R.drawable.marker_log)
//                    markerOptions.anchor(0.5f,0.5f)
//                    mapCtl?.onAddMarker(markerOptions)
//                    mapCtl?.onFocus(latLng)
//                }
//            }
//        })

        // GPSのログ情報が更新されたらマーカーを描画
//        mapRepository.networkLogLocation.observe(fragment, object : Observer<Location?> {
//            override fun onChanged(location: Location?) {
//                if (location != null) {
//                    //Log.v("!!!!!!!!!!","GPSログが更新されました")
//                    val latLng = LatLng(location.latitude, location.longitude)
//                    val markerOptions = getMarkerOptions(latLng, null, com.sp.app.maplib.R.drawable.marker_log2)
//                    markerOptions.anchor(0.5f,0.5f)
//                    mapCtl?.onAddMarker(markerOptions)
//                    mapCtl?.onFocus(latLng)
//                }
//            }
//        })

        // 指定ポイントへのフォーカス
//        mapRepository.selectPoints.observe(fragment, object : Observer<MapLocation?> {
//            override fun onChanged(pointData: MapLocation?) {
//                pointData?.let {
//                    mapCtl?.onFocusSelect(it.point)
//                }
//            }
//        })
//
//        // お気に入りリスト情報の更新検知用
//        dataRepository.favoriteList.observe(fragment, object : Observer<MutableList<FavoriteData>> {
//            override fun onChanged(list: MutableList<FavoriteData>) {
//                mapRepository.setFavoriteData(list)
//
//            }
//        })
//
//        // フォーカスを更新する
//        routeRepository.focusLocation.observe(fragment, object : Observer<LatLng?> {
//            override fun onChanged(location: LatLng?) {
//                if (location != null) {
//                    mapCtl?.onFocusSelect(location)
//                }
//            }
//        })
//
//        routeRepository.nextPointLatLng.observe(fragment, object : Observer<LatLng?> {
//            override fun onChanged(location: LatLng?) {
//                setNextLocateMaker(location)
//            }
//        })
//
//        // ルート線を更新する
//        routeRepository.routesData.observe(fragment, object : Observer<MapRoute?> {
//            override fun onChanged(routeData: MapRoute?) {
//                if (routeData != null) {
//                    // ルート線の表示
//                    setPolyLine(routeData.polylineOptions)
//
//                    // スタート・ゴール・経由地をマーク
//
//                } else {
//                    setPolyLine(null)
//                }
//            }
//        })
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
    private var debugLineView: DebugLineView? = null
    fun setDebugView(debugView: DebugLineView) {
        this.debugLineView = debugView
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // マップ制御系
    ////////////////////////////////////////////////////////////////////////////////////////////////
    var mapCtl: MapInterface? = null

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 地図の長押しで呼ばれる
     */
    fun setSelectLocation(pointData: MapLocation) {
        val address = Constant.getAddress(context!!, pointData.point.latitude, pointData.point.longitude)
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
                context!!,
                latLng,
                null,
                R.drawable.marker_log
            )
            markerOptions.anchor(0.5f,0.5f)
            it.onCurrentLog(markerOptions)
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    fun buttonClickZoomIn(){


    }
    fun buttonClickZoomOut(){


    }
    fun buttonClickThisPosi(){
        if (mapRepository.getLocationCurrent() == null)return
        mapCtl?.onFocusCurrent(mapRepository.getLocationCurrent()!!)
    }
    fun buttonClickCompass() {
        if (mapRepository.getLocationCurrent() == null)return
        mapCtl?.onAngle(mapRepository.getLocationCurrent()!!, 0f)

        //zoom = 17.5f
        //angle = 0f

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////


    // --------------------------------------------------------

    fun onLongClickMap(pointData: MapLocation) {
        val address = Constant.getAddress(context!!, pointData.point.latitude, pointData.point.longitude)
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

        if (angleMode == 0) {
            mapRepository.angleMode.value = 1
            mapCtl?.onTilt(0f)
        } else if (angleMode == 1) {
            mapRepository.angleMode.value = 2
            mapCtl?.onTilt(90f)
        } else {
            mapRepository.angleMode.value = 0

            val location = getCurrentLocation() ?: return
            mapCtl?.onAngle(location, 0f, false)
            mapCtl?.onTilt(0f)
        }

        //zoom = 17.5f
        //angle = 0f
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
            text += "\n" + MagneticSensor.toOrientationString(it) + " : 角度: ${it}°"
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