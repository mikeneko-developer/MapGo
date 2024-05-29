package com.sp.app.maplib

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.sp.app.maplib.data.MapLocation
import com.sp.app.maplib.util.distanceCalc

interface OnMapControllerListener {
    fun onStatus(status: MapController.Companion.MAP_STATUS)
    fun onRequestCurrent()
    fun onLongClickMap(point: MapLocation)
    fun onMarkerClickMap(marker_type: MapController.Companion.MARKER_TYPE, marker_id: String)
    fun onPoiClickMap(point: MapLocation)

    fun onMoveStartMap(isTouch: Boolean)
    fun onMoveMap()
    fun onMoveEndMap(isTouch: Boolean)

    fun onChangeAngle(angle: Float)
    fun onChangeZoom(zoom: Float)
    fun onLastCurrent(latLng: LatLng, point: Point)

}

interface MapInterface {
    fun onClear()
    fun onUpdate()
    fun getMarkers(): List<Marker>
    fun getCurrent(): LatLng?

    fun onCurrent(latLng: LatLng, isAnimation: Boolean = false)       // 現在位置を表示する
    fun onCurrentLog(option: MarkerOptions): Boolean    // ログを表示する

    fun onAddMarker(option: MarkerOptions, key: String? = null): Marker?
    fun onChangeMarkerIcon(marker_id: String, resourceId: Int): Boolean
    fun onDeleteMarker(marker_id: String): Boolean

    fun onSelectZoomAndAngle(latLng: LatLng)
    fun onZoomIn()
    fun onZoomOut()
    fun onZoom(latLng: LatLng, zoom: Float)
    fun onAngle(latLng: LatLng, angle: Float, isAnimation: Boolean = true)
    fun onAngle(angle: Float)

    fun onFocus(latLng: LatLng)         // その他基本フォーカス
    fun onFocusCompass(latLng: LatLng, isAnimation: Boolean): Boolean
    fun onFocusCurrent(latLng: LatLng) // 現在地ボタンを押した時用
    fun onFocusSelect(latLng: LatLng)   // 建物、特定位置から固定する用

    fun onFocusCenter(south: LatLng, north: LatLng)         // 2点間の中心に移動

    fun onAddPolyline(polyOptions: PolylineOptions): Polyline?
    fun onRemovePolyline(polyline_id: String): Boolean

    fun onDisplayArea(width: Int, height: Int)
    fun onLatLogToScreenPosition(latLng: LatLng): Point
    fun onPointToMapLocation(point: Point): LatLng

    fun setMoveCenter(centerMoveX: Double, centerMoveY: Double)

    fun onStartGuide()
    fun onStopGuide()

    fun onTilt(tilt: Float)

}

class MapController(val context: Context, val mMap: GoogleMap, val listener: OnMapControllerListener): MapInterface {
    companion object {
        const  val TAG = "MapController"

        // マップの表示状態
        enum class MAP_STATUS {
            MAP_LOAD,           // マップ情報の読み込み中
            MAP,                // マップ表示状態
            MAP_DIRECT_MOVE,    // 位置情報を直接指定して移動
            TOUCH_MOVE,         // タッチして地図捜査中
            SELECT_FOCUS,       // 指定位置にフォーカスを当て続ける
            GUIDE_FOCUS,        // 案内中用のフォーカス
            ZOOM                // ズームアニメーション中
        }

        enum class MAPLOAD_AND_ZOOM_STEP{
            NONE,           // 初期状態
            MAP_LOAD,       // 地図読み込み中
            MAP_LOADED,     // 地図読み込み完了
            MAP_FIRST_ZOOM, // 最初のズーム
            MAP_ZOOM,       // ズーム開始
            MAP_ZOOM_END,   // ズーム終了
            START_MAP       // 表示開始
        }

        enum class MARKER_TYPE(val type: Int, val info: String){
            SELECT(1, "select"),
            FAVORITE(2, "favorite"),
            POI(3, "poi"),
        }

        const val MARKER_SELECT = "select"
        const val MARKER_FAVORITE = "favorite"

    }

    var STEP = MAPLOAD_AND_ZOOM_STEP.NONE
    private var STATUS = MAP_STATUS.MAP_LOAD

    var currentMarker: Marker? = null

    var markerList = ArrayList<Marker>()
    val currentLog =  ArrayList<Marker>()
    var polylineList = ArrayList<Polyline>()

    var displayWidth = 500
    var displayHeight = 500

    var loadDisplaySize = false
    var centerMoveX = 0.0
    var centerMoveY = 0.0

    var isDebug = true

    init{
        loadDisplaySize = false

        STEP = MAPLOAD_AND_ZOOM_STEP.MAP_LOAD

        // スワイプで平行移動
        mMap.uiSettings.isScrollGesturesEnabled = true

        // ピンチで拡大縮小
        mMap.uiSettings.isZoomGesturesEnabled = true

        // ピンチからの画面回転
        mMap.uiSettings.isRotateGesturesEnabled = true

        //
        //mMap.uiSettings?.isMyLocationButtonEnabled = true

        // ズームボタン
        //mMap.uiSettings?.isZoomGesturesEnabled = true
        //mMap.uiSettings?.isZoomControlsEnabled = true

        // ツールバー
        //mMap.uiSettings.isMapToolbarEnabled = true

        // 視点傾け（ティルト）
        mMap.uiSettings.isTiltGesturesEnabled = true

        // コンパス表示
        mMap.uiSettings.isCompassEnabled = true

        //mMap.uiSettings?.isScrollGesturesEnabledDuringRotateOrZoom = true

        // なんの情報？
        //mMap.setTrafficEnabled(true)


        //mMap.setMyLocationEnabled(true)
        //mMap.setOnMyLocationChangeListener(myLocationChangeListener)

        // Add a marker in Sydney and move the camera
        //val sydney = LatLng(-34.0, 151.0)
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))


        mMap.setOnMapLoadedCallback {
            Log.v(TAG,"STEP:" + STEP + " setOnMapLoadedCallback")
            if(STEP == MAPLOAD_AND_ZOOM_STEP.MAP_LOAD){
                STEP = MAPLOAD_AND_ZOOM_STEP.MAP_LOADED
                listener.onRequestCurrent()
            } else {
                Log.v(TAG, "setOnMapLoadedCallback 地図の読み込み終わり STEP:" + STEP)
            }
        }

        mMap.setOnCameraMoveStartedListener {
            if (!loadDisplaySize) {
                // ディスプレイサイズ読み込み待ち
                return@setOnCameraMoveStartedListener
            }
            if (STEP == MAPLOAD_AND_ZOOM_STEP.MAP_LOAD) return@setOnCameraMoveStartedListener
            Log.v(TAG,"STEP:" + STEP + " setOnCameraMoveStartedListener")


            //Log.v(TAG,"setOnCameraMoveStartedListener " + STATUS)
            Log.v(TAG,"setOnCameraMoveStartedListener it:" + it)
            if (it == 3) {
                Log.v(TAG,"地図の移動開始")
                //listener.onMoveStartMap(false)
            } else if(STATUS == MAP_STATUS.MAP && it == 1){
                STATUS = MAP_STATUS.TOUCH_MOVE
                listener.onStatus(STATUS)
                listener.onMoveStartMap(true)
            } else if(STATUS == MAP_STATUS.SELECT_FOCUS && it == 1){
                STATUS = MAP_STATUS.TOUCH_MOVE
                listener.onStatus(STATUS)
                listener.onMoveStartMap(true)
            } else {
                Log.v(TAG,"地図の操作開始　- 想定外の操作 : STATUS:" + STATUS)
                Log.v(TAG,"地図の操作開始　- 想定外の操作 : it:" + it)
                return@setOnCameraMoveStartedListener
            }

            // コンパスアングル更新
            getCompassAngle()
            // マップの拡大縮小更新
            getMapZoom()
        }

        mMap.setOnCameraMoveListener {

            // コンパスアングル更新
            getCompassAngle()
            // マップの拡大縮小更新
            getMapZoom()

            calcMapPicelData()

            if(STEP != MAPLOAD_AND_ZOOM_STEP.START_MAP) return@setOnCameraMoveListener

            listener.onMoveMap()
        }

        mMap.setOnCameraIdleListener {
            Log.v(TAG,"STEP:" + STEP + " setOnCameraIdleListener ")

            if(STEP == MAPLOAD_AND_ZOOM_STEP.MAP_ZOOM){
                // 開始イベント投げる前の制御
                // タッチイベント処理などを解放
                startEventer()

                // 地図の座標などを取得
                calcMapPicelData()

                STEP = MAPLOAD_AND_ZOOM_STEP.START_MAP

                STATUS = MAP_STATUS.MAP
                listener.onStatus(STATUS)

                return@setOnCameraIdleListener
            }

            if(STEP != MAPLOAD_AND_ZOOM_STEP.START_MAP)return@setOnCameraIdleListener

            if(STATUS == MAP_STATUS.MAP_LOAD || STATUS == MAP_STATUS.SELECT_FOCUS){

            }else if(STATUS == MAP_STATUS.TOUCH_MOVE){
                // コンパスアングル更新
                getCompassAngle()
                // マップの拡大縮小更新
                getMapZoom()
            }else if(STATUS == MAP_STATUS.MAP_DIRECT_MOVE){
                // コンパスアングル更新
                getCompassAngle()
                // マップの拡大縮小更新
                getMapZoom()

                STATUS = MAP_STATUS.MAP
                listener.onStatus(STATUS)

                var latLng =  mMap.cameraPosition.target
                Log.i("MapControllerLocation","setOnCameraMoveCanceledListener()  latitude:" + latLng.latitude + "  longitude:" + latLng.longitude)

            }

            calcMapPicelData()
        }

        mMap.setOnMapClickListener(GoogleMap.OnMapClickListener { point ->
            if(STEP != MAPLOAD_AND_ZOOM_STEP.START_MAP) return@OnMapClickListener

            //Log.v(TAG,"setOnMapClickListener")
        })

        mMap.setOnCameraMoveCanceledListener {
            if(STEP != MAPLOAD_AND_ZOOM_STEP.START_MAP) return@setOnCameraMoveCanceledListener
            Log.v(TAG,"STEP:" + STEP + " setOnCameraMoveCanceledListener")

            if(STATUS == MAP_STATUS.SELECT_FOCUS || STATUS == MAP_STATUS.MAP_LOAD){
                listener.onMoveEndMap(false)

            }else if(STATUS == MAP_STATUS.TOUCH_MOVE){
                listener.onMoveEndMap(true)
            }else if(STATUS == MAP_STATUS.MAP_DIRECT_MOVE){
                STATUS = MAP_STATUS.MAP
                listener.onStatus(STATUS)
                listener.onMoveEndMap(false)

                var latLng =  mMap.cameraPosition.target
                Log.i("MapControllerLocation","setOnCameraMoveCanceledListener()  latitude:" + latLng.latitude + "  longitude:" + latLng.longitude)


            }
            calcMapPicelData()
        }
    }

    private fun startEventer() {
        mMap.setOnMapLongClickListener(object : GoogleMap.OnMapLongClickListener {
            override fun onMapLongClick(latLng: LatLng) {
                if(STEP != MAPLOAD_AND_ZOOM_STEP.START_MAP) return

                val point = MapLocation(
                    "選択位置",
                    latLng
                )
                point.newPosition = true

                if (STATUS == MAP_STATUS.SELECT_FOCUS) {
                    listener.onLongClickMap(point)
                } else if (STATUS == MAP_STATUS.MAP || STATUS == MAP_STATUS.MAP_DIRECT_MOVE || STATUS == MAP_STATUS.TOUCH_MOVE) {
                    listener.onLongClickMap(point)
                }
            }
        })

        mMap.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(marker: Marker): Boolean {
                if(STEP != MAPLOAD_AND_ZOOM_STEP.START_MAP) return false

                marker.tag?.let {
                    val tag = it as String

                    Log.v(TAG, "id:" + marker.id)
                    if (tag == MARKER_TYPE.SELECT.info) {
                        Log.v(TAG, "選択位置を選択")
                        listener.onMarkerClickMap(MARKER_TYPE.SELECT, marker.id)
                        return true
                    } else if (tag == MARKER_TYPE.FAVORITE.info) {
                        Log.v(TAG, "お気に入りを選択")
                        listener.onMarkerClickMap(MARKER_TYPE.FAVORITE, marker.id)
                        return true
                    } else if (marker.title!! == "選択位置") {
                        Log.v(TAG, "選択位置")
                        //mapRepository.selectLatLng.postValue(selectLatLng)
                        //navigator?.openSelectMap()
                        //mapRepository.onClickMarker(1, marker)
                        return true
                    }
                }

                return false
            }
        })

        mMap.setOnPoiClickListener(object: GoogleMap.OnPoiClickListener {
            override fun onPoiClick(pointOfInterest: PointOfInterest) {
                if(STEP != MAPLOAD_AND_ZOOM_STEP.START_MAP) return

                Log.v(TAG, "setOnPoiClickListener 既存マーカータップ name:" + pointOfInterest!!.name)
                var placeIdLandMark = pointOfInterest!!.placeId

                val point = MapLocation(
                    pointOfInterest.name,
                    pointOfInterest.latLng
                )
                point.newPosition = true

                if (STATUS == MAP_STATUS.SELECT_FOCUS) {
                    listener.onPoiClickMap(point)
                } else if (STATUS == MAP_STATUS.MAP || STATUS == MAP_STATUS.MAP_DIRECT_MOVE || STATUS == MAP_STATUS.TOUCH_MOVE) {
                    listener.onPoiClickMap(point)
                }
            }
        })
    }


    private fun clear() {
        currentLog.clear()
        mMap.clear()
    }

    private fun calcMapPicelData() {
        //Log.i(TAG, "bearing:" + mMap.cameraPosition.bearing)
        //Log.i(TAG, "tilt:" + mMap.cameraPosition.tilt)
        //Log.i(TAG, "latitude:" + mMap.cameraPosition.target.latitude)
        //Log.i(TAG, "longitude:" + mMap.cameraPosition.target.longitude)

        if (currentMarker != null) {
            val latLng = currentMarker!!.position
            val point = onLatLogToScreenPosition(latLng)
            //Log.i(TAG, "longitude:" + mMap.cameraPosition.target.longitude)
            listener.onLastCurrent(latLng, point)
        }
    }

    private fun changeStatus(status: MapController.Companion.MAP_STATUS) {
        STATUS = status
        listener.onStatus(STATUS)
    }
    
    private fun getCompassAngle() {
        if(STEP != MAPLOAD_AND_ZOOM_STEP.START_MAP)return

        if(STEP == MAPLOAD_AND_ZOOM_STEP.START_MAP){
            angle = mMap.cameraPosition.bearing
            listener.onChangeAngle(mMap.cameraPosition.bearing)
        }
    }

    private fun getMapZoom() {
        if(STEP != MAPLOAD_AND_ZOOM_STEP.START_MAP)return

        if(STATUS == MAP_STATUS.ZOOM || STATUS == MAP_STATUS.MAP_DIRECT_MOVE){

        }else {
            //　位置情報更新が終わったら各宿情報を取得する
                zoom = mMap.cameraPosition.zoom
            listener.onChangeZoom(mMap.cameraPosition.zoom)
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    // 画像データ管理
    /////////////////////////////////////////////////////////////////////////////////////////
    var imageMap = mutableMapOf<Int, Bitmap?>()

    private fun getImage(resource_id: Int): Bitmap? {
        if(imageMap.containsKey(resource_id)){
            var bitmap = imageMap.get(resource_id)

            if(bitmap != null) {
                return bitmap
            }
        }

        val r: Resources = context.getResources()
        var bitmap = BitmapFactory.decodeResource(r, resource_id)

        imageMap.set(resource_id, bitmap)

        return bitmap
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    // 現在位置更新
    /////////////////////////////////////////////////////////////////////////////////////////
    private fun updateCurrentMarker(latLng: LatLng) {

        if(currentMarker == null) {
            val options = MarkerOptions()
            //options.title(text)
            options.position(latLng)

            var bmp = getImage(R.drawable.marker_current)
            bmp?.let {
                var icon = BitmapDescriptorFactory.fromBitmap(bmp)
                //var icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                options.icon(icon)
            }
            options.anchor(0.5f, 0.5f)//　サイズ指定
            currentMarker = onAddMarker(options)

        }else {
            currentMarker!!.position = latLng
            currentMarker!!.zIndex = 100f
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    //  ズーム、アングル、チルトの再描画
    /////////////////////////////////////////////////////////////////////////////////////////
    private var zoom = 17.5f
    private var angle = 0f
    private var tilt = 0f

    private fun reloadZoomAndAngleAndTilt(current: LatLng, isAnimation: Boolean = true){
        val factory = CameraUpdateFactory.newCameraPosition(
            CameraPosition(
                current,
                zoom,
                tilt,
                angle
            )
        )

        if (STATUS == MAP_STATUS.TOUCH_MOVE
            || STATUS == MAP_STATUS.SELECT_FOCUS) {
            return
        }

        if (isAnimation) {
            mMap.animateCamera(factory)
        } else {
            mMap.moveCamera(factory)
        }
    }

    fun setZoomAndAngle(current: LatLng, nextZoom: Float, nextAngle: Float, isAnimation: Boolean = true){
        var sabunZoom = zoom - nextZoom

        zoom = nextZoom
        angle = nextAngle

        var _latLng = calcZoomMoveCenterToLatLng(current, sabunZoom)
        if (nextZoom < zoom) {
            //_latLng = calcZoomOutMoveCenterToLatLng(current)
        }

        reloadZoomAndAngleAndTilt(current, isAnimation)
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    private fun setFocus(latLng: LatLng, isAnimation: Boolean = true) {
        Log.i("MapControllerLocation","setFocus()  latitude:" + latLng.latitude + "  longitude:" + latLng.longitude)
        reloadZoomAndAngleAndTilt(latLng, isAnimation)
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    private fun calcMoveCenterToLatLng(latLng: LatLng): LatLng {

        val point = onLatLogToScreenPosition(latLng)
        var newLatLng = onPointToMapLocation(Point(
            ((point.x + centerMoveX).toInt()), ((point.y + centerMoveY).toInt())))

        return newLatLng
    }

    private fun calcHalfMoveCenterToLatLng(latLng: LatLng): LatLng {

        val point = onLatLogToScreenPosition(latLng)
        var newLatLng = onPointToMapLocation(Point(
            ((point.x + centerMoveX).toInt()), ((point.y + (centerMoveY/2)).toInt())))

        return newLatLng
    }

    private fun calcZoomMoveCenterToLatLng(latLng: LatLng, sabunZoom: Float): LatLng {
        val point = onLatLogToScreenPosition(latLng)



        var newLatLng = onPointToMapLocation(Point(
            ((point.x + centerMoveX).toInt()), ((point.y + centerMoveY).toInt())))

        return newLatLng
    }

    private fun calcZoomOutMoveCenterToLatLng(latLng: LatLng): LatLng {

        val point = onLatLogToScreenPosition(latLng)
        var newLatLng = onPointToMapLocation(Point(
            ((point.x - centerMoveX).toInt()), ((point.y - (centerMoveY/4)).toInt())))

        return latLng
    }

    /**
     * 2点間の中心にカメラが移動し、全体が収まるように移動する
     */
    private fun setTwoPointCenter(_south: LatLng, _north: LatLng) {
        Log.i(TAG, "south.latitude:" + _south.latitude)
        Log.i(TAG, "north.latitude:" + _north.latitude)
        Log.i(TAG, "south.longitude:" + _south.longitude)
        Log.i(TAG, "north.longitude:" + _north.longitude)

        val south = calcHalfMoveCenterToLatLng(_south)
        val north = calcHalfMoveCenterToLatLng(_north)


        var p1 =0.0
        var p3 =0.0
        if (south.latitude < north.latitude) {
            p1 = south.latitude
            p3 = north.latitude
        } else {
            p1 = north.latitude
            p3 = south.latitude
        }

        var p2 =0.0
        var p4 =0.0
        if (south.longitude < north.longitude) {
            p2 = south.longitude
            p4 = north.longitude
        } else {
            p2 = north.longitude
            p4 = south.longitude
        }

        val distance1 = distanceCalc(p1, p2, p3, p2)
        val distance2 = distanceCalc(p1, p2, p1, p4)

        Log.i(TAG, "distance1:" + distance1)
        Log.i(TAG, "distance2:" + distance2)
        var padding = 500
        if (distance1 < distance2) {
            padding = 200
        }

        try {
            val australiaBounds = LatLngBounds(
                LatLng(p1, p2),  // SW bounds 南西
                LatLng(p3, p4), // NE bounds　東北
                //south,
                //north,
            )

            var width = 500
            if (displayWidth < displayHeight) {
                width = displayWidth
            } else {
                width = displayHeight
            }
            val height = width

            //val bounds = CameraUpdateFactory.newLatLngBounds(australiaBounds, padding)
            val bounds = CameraUpdateFactory.newLatLngBounds(
                australiaBounds, width, height, width/ 20)

            mMap.moveCamera(bounds)

        }catch(e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    // 現在地のログ
    private fun setCurrentLog(options: MarkerOptions): Boolean {
        val marker = mMap.addMarker(options)

        if (marker == null) {
            return false
        }
        currentLog.add(marker)

        if(currentLog.size > 100000){
            val lastMarker = currentLog[0]
            currentLog.removeAt(0)

            lastMarker.remove()
        }
        return true
    }
    /////////////////////////////////////////////////////////////////////////////////////////

    override fun onClear() {
        clear()
    }

    override fun onUpdate() {
    }

    override fun onCurrent(latLng: LatLng, isAnimation: Boolean) {
        if (STEP == MAPLOAD_AND_ZOOM_STEP.MAP_LOAD) return

        Log.v(TAG,"STEP:" + STEP + " onCurrent")

        // 現在位置の情報が更新されたので地図上に反映する
        updateCurrentMarker(latLng)

        if (STEP == MAPLOAD_AND_ZOOM_STEP.MAP_LOADED) {
            // 最初の読み込みなので拡大表示する
            STEP = MAPLOAD_AND_ZOOM_STEP.MAP_ZOOM

            setFocus(latLng)
            //setZoomAndAngle(latLng, zoom, angle)
        } else {
            // 地図が使用可能な状態なので、フォーカスのみ移動
            if (STATUS == MAP_STATUS.TOUCH_MOVE
                || STATUS == MAP_STATUS.SELECT_FOCUS) {
                return
            }

            // フォーカスを移動
            setFocus(latLng, isAnimation)
        }
    }

    override fun onCurrentLog(options: MarkerOptions): Boolean {
        return setCurrentLog(options)
    }

    override fun onAddMarker(options: MarkerOptions, key: String?): Marker? {
        val marker = mMap.addMarker(options)

        marker?.let {
            it.tag = key
            markerList.add(it)
        }

        return marker
    }

    override fun onChangeMarkerIcon(marker_id: String, resourceId: Int): Boolean {
        for(marker in markerList) {
            if (marker.id == marker_id) {
                getImage(resourceId)?.let {
                    val icon = BitmapDescriptorFactory.fromBitmap(it)
                    marker.setIcon(icon)
                    return true
                }
            }
        }

        return false
    }

    override fun onDeleteMarker(marker_id: String): Boolean {
        markerList.forEach {
            if (it.id == marker_id) {
                it.remove()
                markerList.remove(it)
                return true
            }
        }

        return false
    }

    override fun getMarkers(): List<Marker> {
        return markerList
    }

    override fun getCurrent(): LatLng? {
        currentMarker?.let {
            return it.position
        }
        return null
    }

    override fun onSelectZoomAndAngle(latLng: LatLng) {
        if (STEP != MAPLOAD_AND_ZOOM_STEP.START_MAP) return

        if (STATUS == MAP_STATUS.SELECT_FOCUS) {

        } else if (STATUS != MAP_STATUS.TOUCH_MOVE) {
            setZoomAndAngle(latLng, zoom, angle)
        } else {
            if (mMap.cameraPosition.target.latitude != latLng.latitude
                || mMap.cameraPosition.target.longitude != latLng.longitude) {
                setZoomAndAngle(latLng, zoom, angle)
            }
        }
    }

    override fun onZoomIn() {
        if (STEP != MAPLOAD_AND_ZOOM_STEP.START_MAP) return

        if(STATUS == MAP_STATUS.SELECT_FOCUS || STATUS == MAP_STATUS.TOUCH_MOVE){

        }else {
            changeStatus(MAP_STATUS.MAP_DIRECT_MOVE)
            listener.onStatus(STATUS)
        }

        val nextZoom = zoom + Constant.ZOOM_ONE_SIZE
        setZoomAndAngle(mMap.cameraPosition.target, nextZoom, angle)
    }

    override fun onZoomOut() {
        if(STEP != MAPLOAD_AND_ZOOM_STEP.START_MAP)return

        if(STATUS == MAP_STATUS.SELECT_FOCUS || STATUS == MAP_STATUS.TOUCH_MOVE){

        }else {
            changeStatus(MAP_STATUS.MAP_DIRECT_MOVE)
            listener.onStatus(STATUS)
        }

        val nextZoom = zoom - Constant.ZOOM_ONE_SIZE
        setZoomAndAngle(mMap.cameraPosition.target, nextZoom, angle)
    }

    override fun onAngle(latLng: LatLng, angle: Float, isAnimation: Boolean) {
        setZoomAndAngle(latLng, zoom, angle, isAnimation)
    }

    override fun onAngle(angle: Float) {
        this.angle = angle
    }

    override fun onZoom(latLng: LatLng, zoom: Float) {
        setZoomAndAngle(latLng, zoom, angle, true)
    }

    override fun onFocusCompass(latLng: LatLng, isAnimation: Boolean): Boolean {
        if(STEP != MAPLOAD_AND_ZOOM_STEP.START_MAP)return true
        Log.v(TAG,"STATUS:" + STATUS + " onFocusCompass")

        setFocus(latLng, false)

        return (mMap.cameraPosition.bearing != angle)
    }

    /**
     * 現在地指定でのフォーカス実行用
     */
    override fun onFocusCurrent(latLng: LatLng) {
        if(STEP != MAPLOAD_AND_ZOOM_STEP.START_MAP)return
        Log.v(TAG,"STATUS:" + STATUS + " onFocusCurrent")

        if (STATUS != MAP_STATUS.GUIDE_FOCUS) {
            changeStatus(MAP_STATUS.MAP_DIRECT_MOVE)
        }
        setFocus(latLng, false)
    }

    override fun onFocusSelect(latLng: LatLng) {
        if(STEP != MAPLOAD_AND_ZOOM_STEP.START_MAP)return
        Log.v(TAG,"STATUS:" + STATUS + " onFocusSelect")

        if (STATUS == MAP_STATUS.GUIDE_FOCUS) {
            return
        }

        changeStatus(MAP_STATUS.SELECT_FOCUS)
        setFocus(latLng, false)

    }

    override fun onFocusCenter(south: LatLng, north: LatLng) {
        if(STEP != MAPLOAD_AND_ZOOM_STEP.START_MAP)return

        changeStatus(MAP_STATUS.SELECT_FOCUS)
        setTwoPointCenter(south, north)
    }

    override fun onFocus(latLng: LatLng) {
        if(STEP != MAPLOAD_AND_ZOOM_STEP.START_MAP)return
        Log.v(TAG,"STATUS:" + STATUS + " onFocus")

        if (STATUS == MAP_STATUS.SELECT_FOCUS) {
            // 通常のフォーカスでは変更させない

        } else if(STATUS == MAP_STATUS.TOUCH_MOVE){
            //changeStatus(MAP_STATUS.MAP_DIRECT_MOVE)
            //setFocus(latLng)
        } else if(STATUS == MAP_STATUS.MAP || STATUS == MAP_STATUS.MAP_DIRECT_MOVE){
            changeStatus(MAP_STATUS.MAP_DIRECT_MOVE)
            setFocus(latLng, true)
        }

    }

    override fun onAddPolyline(polyOptions: PolylineOptions): Polyline {
        val polyline = mMap.addPolyline(polyOptions)
        polyline?.let {
            polylineList.add(polyline)
        }

        return polyline
    }

    override fun onRemovePolyline(polyline_id: String): Boolean {
        for(polyline in polylineList) {
            if(polyline.id == polyline_id) {
                polyline.remove()
                polylineList.remove(polyline)
                return true
            }
        }

        return false
    }

    override fun onDisplayArea(width: Int, height: Int) {
        if (width != -1) displayWidth = width
        if (height != -1) displayHeight = height
    }

    override fun onLatLogToScreenPosition(latLng: LatLng): Point {
        val point = mMap.projection.toScreenLocation(latLng)
        return Point(point.x*2, point.y*2)
    }

    override fun onPointToMapLocation(point: Point): LatLng {
       val latLng =  mMap.projection.fromScreenLocation(Point(point.x/2, point.y/2))
        return LatLng(latLng.latitude, latLng.longitude)
    }

    override fun setMoveCenter(centerMoveX: Double, centerMoveY: Double) {
        Log.v(TAG,"STATUS:" + STATUS + " setMoveCenter")
        loadDisplaySize = true
        this.centerMoveX = centerMoveX
        this.centerMoveY = centerMoveY
    }

    override fun onStartGuide() {
        changeStatus(MAP_STATUS.GUIDE_FOCUS)
        listener.onRequestCurrent()
    }

    override fun onStopGuide() {
        changeStatus(MAP_STATUS.MAP)
        listener.onRequestCurrent()
    }

    override fun onTilt(tilt: Float) {
        val isAnim = this.tilt != tilt
        this.tilt = tilt

        if (STATUS == MAP_STATUS.TOUCH_MOVE
            || STATUS == MAP_STATUS.SELECT_FOCUS) {
            return
        }
        val latLng = getCurrent()
        latLng?.let {
            setFocus(it, isAnim)
        }

    }


}