package net.mikemobile.navi.ui.map

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.location.Location
import android.os.Handler
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import net.mikemobile.databindinglib.base.BaseFragment
import net.mikemobile.databindinglib.base.observeOnChangedForever
import net.mikemobile.navi.R
import net.mikemobile.navi.data.old.FavoriteData
import net.mikemobile.navi.data.map.MapLocation
import net.mikemobile.navi.data.map.MapRoute
import net.mikemobile.navi.repository.*
import net.mikemobile.navi.repository.data.TwoPoint
import net.mikemobile.navi.ui.util.CharacterView
import net.mikemobile.navi.ui.util.DebugLineView
import net.mikemobile.navi.util.Constant
import net.mikemobile.navi.util.distanceCalc


class MapViewModel(
    val mapRepository: MapRepository,
    val routeRepository: RouteRepository,
    val guideRepository: GuideRepository,
    val dataRepository: DataRepository,
    var roboConnectRepository: RoboConnectRepository
) : ViewModel(), OnMapReadyCallback, OnMapListener, OnMapControllerListener {

    companion object {
        const val TAG = "MapViewModel"

        const val MARKER_SELECT = "select"
        const val MARKER_FAVORITE = "favorite"
    }

    var context: Context? = null
    var firstUpdateMap = false
    var navigator: MapFragmentNavigator? = null
    val handler = Handler()


    var centerMoveX = 0.0
    var centerMoveY = 0.0
    ///////////////////////////////////////////////////

    var say_text = MutableLiveData<String>().apply {
        value = ""
    }
    var nextPointDistance = MutableLiveData<String>().apply {
        value = ""
    }

    ///////////////////////////////////////////////////
    var view_zoom: LiveData<Float> = mapRepository.mapZoom
    var view_angle: LiveData<Float> = mapRepository.angle
    var view_mapStatus: LiveData<String> = mapRepository.mapStatusString

    var view_thisGps: LiveData<String> = mapRepository.thisGps
    var view_logGps: LiveData<String> = mapRepository.logGps
    var view_etowrk: LiveData<String> = mapRepository.thisNetwork
    var view_logNetowrk: LiveData<String> = mapRepository.logNetwork

    //var view_log_text: LiveData<Spanned> = mapRepository.logText

    var view_log_text = MutableLiveData<String>().apply {
        value = ""
    }

    var angleMode: LiveData<Int> = mapRepository.angleMode
    var realTimeAngle: LiveData<Float> = mapRepository.realTimeAngle

    ///////////////////////////////////////////////////
    fun initialize(context: Context) {
        this.context = context

        mapRepository.isMapLocationEnable = true
        firstUpdateMap = true
        mapRepository.startLocation()

        // 現在位置が更新されたら取得する
        roboConnectRepository.say_text.observeOnChangedForever(object : Observer<String> {
            override fun onChanged(say: String) {
                say_text.postValue(say)
            }
        })

        // 現在位置が更新されたら取得する
        mapRepository.currentPositionLiveData.observeOnChangedForever(object : Observer<LatLng?> {
            override fun onChanged(location: LatLng?) {
                if (location != null) {
                    setCurrentLocation(location)
                }
            }
        })

        // GPSが更新されたらマーカーを描画
        mapRepository.gpsLocation.observeOnChangedForever(object : Observer<Location?> {
            override fun onChanged(location: Location?) {
                if (location != null) {
                    //Log.v("!!!!!!!!!!","GPSが更新されました")
                    val latLng = LatLng(location.latitude, location.longitude)
                    val markerOptions = getMarkerOptions(latLng, null, R.drawable.marker_gps)
                    markerOptions.anchor(0.5f,0.5f)
                    mapCtl?.onAddMarker(markerOptions)
                    mapCtl?.onFocus(latLng)
                }
            }
        })

        // 基地局位置情報が更新されたらマーカーを描画
        mapRepository.networkLocation.observeOnChangedForever(object : Observer<Location?> {
            override fun onChanged(location: Location?) {
                if (location != null) {
                    //Log.v("!!!!!!!!!!","GPSログが更新されました")
                    val latLng = LatLng(location.latitude, location.longitude)
                    val markerOptions = getMarkerOptions(latLng, null, R.drawable.marker_network)
                    markerOptions.anchor(0.5f,0.5f)
                    mapCtl?.onAddMarker(markerOptions)
                    mapCtl?.onFocus(latLng)
                }
            }
        })
    }

    fun resume(fragment: BaseFragment) {
        mapRepository.isMapLocationEnable = true
        mapRepository.setOnMapListener(this)

        guideRepository.guideMode.observe(fragment, object : Observer<String?> {
            override fun onChanged(guideMode: String?) {
                if (guideMode != null) {
                    mapCtl?.onStartGuide()
                } else {
                    mapCtl?.onStopGuide()
                }
            }
        })

        mapRepository.twoPointLiveData.observe(fragment, object : Observer<TwoPoint?> {
            override fun onChanged(twoPoint: TwoPoint?) {
                twoPoint?.let {
                    mapCtl?.onFocusCenter(twoPoint.neLatLng, twoPoint.wsLatLng)
                }
            }
        })
        //////////////////////////////////////////////////////////////////////////////
        // GPSのログ情報が更新されたらマーカーを描画
        mapRepository.gpsLogLocation.observe(fragment, object : Observer<Location?> {
            override fun onChanged(location: Location?) {
                if (location != null) {
                    //Log.v("!!!!!!!!!!","GPSログが更新されました")
                    val latLng = LatLng(location.latitude, location.longitude)
                    val markerOptions = getMarkerOptions(latLng, null, R.drawable.marker_log)
                    markerOptions.anchor(0.5f,0.5f)
                    mapCtl?.onAddMarker(markerOptions)
                    mapCtl?.onFocus(latLng)
                }
            }
        })

        // GPSのログ情報が更新されたらマーカーを描画
        mapRepository.networkLogLocation.observe(fragment, object : Observer<Location?> {
            override fun onChanged(location: Location?) {
                if (location != null) {
                    //Log.v("!!!!!!!!!!","GPSログが更新されました")
                    val latLng = LatLng(location.latitude, location.longitude)
                    val markerOptions = getMarkerOptions(latLng, null, R.drawable.marker_log2)
                    markerOptions.anchor(0.5f,0.5f)
                    mapCtl?.onAddMarker(markerOptions)
                    mapCtl?.onFocus(latLng)
                }
            }
        })

        // 指定ポイントへのフォーカス
        mapRepository.selectPoints.observe(fragment, object : Observer<MapLocation?> {
            override fun onChanged(pointData: MapLocation?) {
                pointData?.let {
                    mapCtl?.onFocusSelect(it.point)
                }
            }
        })

        // お気に入りリスト情報の更新検知用
        dataRepository.favoriteList.observe(fragment, object : Observer<MutableList<FavoriteData>> {
            override fun onChanged(list: MutableList<FavoriteData>) {
                mapRepository.setFavoriteData(list)

            }
        })

        // フォーカスを更新する
        routeRepository.focusLocation.observe(fragment, object : Observer<LatLng?> {
            override fun onChanged(location: LatLng?) {
                if (location != null) {
                    mapCtl?.onFocusSelect(location)
                }
            }
        })

        routeRepository.nextPointLatLng.observe(fragment, object : Observer<LatLng?> {
            override fun onChanged(location: LatLng?) {
                setNextLocateMaker(location)
            }
        })

        // ルート線を更新する
        routeRepository.routesData.observe(fragment, object : Observer<MapRoute?> {
            override fun onChanged(routeData: MapRoute?) {
                if (routeData != null) {
                    // ルート線の表示
                    setPolyLine(routeData.polylineOptions)

                    // スタート・ゴール・経由地をマーク

                } else {
                    setPolyLine(null)
                }
            }
        })
    }

    fun pause(fragment: BaseFragment) {
        mapRepository.isMapLocationEnable = false
        mapRepository.setOnMapListener(null)

        mapRepository.twoPointLiveData.removeObservers(fragment)
        mapRepository.gpsLogLocation.removeObservers(fragment)
        mapRepository.networkLogLocation.removeObservers(fragment)
        mapRepository.selectLongTapPoint.removeObservers(fragment)

        routeRepository.focusLocation.removeObservers(fragment)
        routeRepository.nextPointLatLng.removeObservers(fragment)
        routeRepository.routesData.removeObservers(fragment)
    }

    fun destroy(fragment: BaseFragment) {
        mapRepository.isMapLocationEnable
        mapRepository.stopLocation()


        mapRepository.currentPositionLiveData.removeObservers(fragment)
        mapRepository.gpsLocation.removeObservers(fragment)
        mapRepository.networkLocation.removeObservers(fragment)
        mapCtl?.onClear()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private var debugLineView: DebugLineView? = null
    fun setDebugView(debugView: DebugLineView) {
        this.debugLineView = debugView
    }

    private var characterView: CharacterView? = null
    fun setCharacterView(characterView: CharacterView) {
        this.characterView = characterView
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    var mapCtl: MapInterface? = null
    override fun onMapReady(googleMap: GoogleMap) {
        mapCtl?.onClear()

        context?.let {
            mapCtl = MapController(it, googleMap, this)
            setMapViewSize(mapRepository.windowWidth,mapRepository.windowHeight)
        }

        // 現在位置が取得できるなら取得しておく

        if (mapRepository.getLocationCurrent() == null)return
        //mapCtl?.onCurrent(mapRepository.getLocationCurrent()!!)
    }

    fun setMapViewSize(width: Int, height: Int) {
        characterView?.let {
            val startPoint = Point(0,height/10)
            val endPoint = Point(width,height - (height/ 5 * 2))
            it.setMapEnableArea(startPoint, endPoint)

            val areaWidth = endPoint.x - startPoint.x
            val areaHeight = endPoint.y - startPoint.y

            mapCtl?.onDisplayArea(areaWidth, areaHeight / 3 * 2)

            //centerMoveX = 0.0
            //centerMoveY = (areaHeight/2 + startPoint.y/2).toDouble()

            //centerMoveY = 700.0

            mapCtl?.setMoveCenter(centerMoveX, centerMoveY)

            debugLineView?.let {
                //it.setMapEnableArea(startPoint, endPoint)
            }
        }






    }

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
    fun getMarkerOptions(latLng: LatLng, text: String?, resource: Int, alpha: Float = 1f): MarkerOptions {
        val options = MarkerOptions()
        if (text != null)options.title(text)
        options.position(latLng)
        getImage(resource)?.let {
            var icon = BitmapDescriptorFactory.fromBitmap(it)
            //var icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
            options.icon(icon)
        }
        return options
    }

    fun setCurrentLocation(latLng: LatLng) {
        mapCtl?.onCurrent(latLng)

        nextMarker?.let {
            setDistancePolyline(latLng, it.position)
        }

        routeRepository.setCurrentPoint(latLng)

        // Serviceを作るまでの仮
        guideRepository.calcChecKPointDistance(latLng)

        mapCtl?.let {
            val point = it.onLatLogToScreenPosition(latLng)
            Log.i(TAG, "Current() point:" + point)
        }

    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    var nextMarker: Marker? = null
    fun setNextLocateMaker(latLng: LatLng?){
        if(latLng == null){
            if(nextMarker != null){
                mapCtl?.onDeleteMarker(nextMarker!!.id)
                nextMarker!!.remove()
                nextMarker = null
            }

            if(nextPoliLine != null) {
                mapCtl?.onRemovePolyline(nextPoliLine!!.id)
                nextPoliLine!!.remove()
                nextPoliLine = null
            }
        }else {
            // 現在位置

            if (nextMarker == null) {
                val options = createMarkerOptions("次のポイント", latLng, R.drawable.ic_next)
                nextMarker = mapCtl?.onAddMarker(options)
            } else {
                nextMarker!!.position = latLng
            }

            mapCtl?.getCurrent()?.let {
                setDistancePolyline(it, latLng)
            }

            mapCtl?.let {
                val point = it.onLatLogToScreenPosition(latLng)

                Log.i(TAG, "nextPoint point:" + point)

            }
        }
    }

    var nextPoliLine:Polyline? = null
    fun setDistancePolyline(current: LatLng, next: LatLng) {
        nextPoliLine?.let {
            it.remove()
        }

        val polyOptions = PolylineOptions()
        polyOptions.add(current)
        polyOptions.add(next)
        polyOptions.color(Color.RED)
        polyOptions.width(3f)
        polyOptions.geodesic(true) //true:大圏コース　false:直線

        nextPoliLine = mapCtl?.onAddPolyline(polyOptions)

        var distance = distanceCalc(current.latitude,current.longitude,next.latitude, next.longitude)

        nextPointDistance.postValue("次の地点までの距離:" + distance + "m")
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    var polylineId: String? = null
    fun setPolyLine(polylineOptions: PolylineOptions?){

        if(polylineOptions == null){
            polylineId?.let {
                mapCtl?.onRemovePolyline(it)
            }

        }else {
            val polyline = mapCtl?.onAddPolyline(polylineOptions)
            polyline?.let {

                if(polylineId != null) {
                    mapCtl?.onRemovePolyline(polylineId!!)
                }

                polylineId = it.id
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    fun buttonClickZoomIn(){
        mapCtl?.onZoomIn()

    }
    fun buttonClickZoomOut(){
        mapCtl?.onZoomOut()

    }
    fun buttonClickThisPosi(){
        if (mapRepository.getLocationCurrent() == null)return
        mapCtl?.onFocusCurrent(mapRepository.getLocationCurrent()!!)
    }
    fun buttonClickCompass() {
        if (mapRepository.getLocationCurrent() == null)return

        val angleMode = angleMode.value
        if (angleMode == null) {
            return
        }

        if (angleMode == 0) {
            mapRepository.angleMode.value = 1
        } else {
            mapRepository.angleMode.value = 0

            val location = getCurrentLocation() ?: return
            mapCtl?.onAngle(location, 0f, true)
        }

//        if (angleMode == 0) {
//            mapRepository.angleMode.value = 1
//        } else if (angleMode == 1) {
//            mapRepository.angleMode.value = 2
//        } else {
//            mapRepository.angleMode.value = 0
//
//            val location = getCurrentLocation() ?: return
//            mapCtl?.onAngle(location, 0f, true)
//        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // マーカー共通処理

    /**
     * MarkerOptionsの作成
     */
    private fun createMarkerOptions(title: String, point: LatLng, resourceId: Int, alpha:Float = 1f): MarkerOptions{
        val options = MarkerOptions()
        options.title(title)
        options.position(point)
        options.alpha(alpha)
        options.anchor(0.5f, 0.85f)//　中心位置指定

        if (resourceId == -1) {
            var icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
            options.icon(icon)
        } else {
            val bmp = getImage(resourceId)
            bmp?.let {
                val icon = BitmapDescriptorFactory.fromBitmap(bmp)
                //var icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                options.icon(icon)
            }
        }

        return options
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 選択マーカーを追加する処理
     * @param pointData
     */
    override fun onAddMarker(pointData: MapLocation) {
        Log.i(TAG + " ADD_MAKER", "onAddMaker point name:" + pointData.name)

        val list = mapRepository.getSelectPointList()
        if (pointData.haveGoal && list.size > 0) {
            // 目的地に設定するので現在のゴールを変更する
        }

        val resourceId: Int = if (pointData.haveGoal) {
            R.drawable.ic_goal
        } else if (pointData.name == "スタート地点") {
            R.drawable.ic_start
        } else {
            R.drawable.ic_via
        }
        val options = createMarkerOptions(pointData.name, pointData.point, resourceId)


        val maker = mapCtl?.onAddMarker(options, MARKER_SELECT)
        maker?.let {
            pointData.marker_id = it.id
        }

        // 追加したマーカーをリポジトリーに登録する
        if (pointData.name != "スタート地点") {
            //mapRepository.addSelectPoint(pointData)

            mapRepository.updateSelectPoint(pointData, true)
        }


        mapCtl?.let {
            val point = it.onLatLogToScreenPosition(pointData.point)
            Log.i(TAG, "ルート地点マーカー point:" + point)

        }
    }

    /**
     * 選択マーカーの状態を変更する処理
     */
    override fun onChangeMarker(pointData: MapLocation) {
        // 先にゴール指定のデータのアイコンを経由地に変更する
        val list = mapRepository.getSelectPointList()

        if(pointData.haveGoal) {
            //現在の目的地データ
            pointData.marker_id?.let {
                mapCtl?.onChangeMarkerIcon(it, R.drawable.ic_goal) }


        } else {
            // ゴールから経由地変更

            pointData.marker_id?.let {
                mapCtl?.onChangeMarkerIcon(it, R.drawable.ic_via) }

        }
        mapRepository.updateSelectPoint(pointData, false)
    }

    /**
     * 選択マーカーを削除する処理
     * @param pointData
     */
    override fun onRemoveMarker(pointData: MapLocation) {
        var res = true
        if (pointData.marker_id == null || mapCtl == null) {
            // marker_idがない場合、マップに追加されていないため、削除したことにして返す
        } else {
            res = mapCtl!!.onDeleteMarker(pointData.marker_id!!)
        }

        // 最後の
        val list = mapRepository.getSelectPointList()

        // 現在の位置を取得
        var p1 = -1
        for(i in 0 until list.size) {
            if (list[i].marker_id == pointData.marker_id) {
                p1 = i
                break
            }
        }

        if (list.size > 1) {
            // 最終的なゴールの位置を取得
            var goalPoint: MapLocation
            if (p1 == list.size - 1) {
                goalPoint = list[list.size - 2]
            } else {
                goalPoint = list[list.size - 1]
            }

            goalPoint.marker_id?.let { mapCtl?.onChangeMarkerIcon(it, R.drawable.ic_goal) }
        }

        //mapRepository.deleteSelectPoint(res, pointData)
        //mapRepository.updateSelectPoint(pointData)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // お気に入りマーカー

    override fun onAddFavoriteMarker(pointData: MapLocation) {
        Log.i(TAG + " ADD_MAKER", "onAddMaker point name:" + pointData.name)

        val options = createMarkerOptions(pointData.name, pointData.point, R.drawable.ic_favorite)
        val maker = mapCtl?.onAddMarker(options, MARKER_FAVORITE)
        maker?.let {
            pointData.marker_id = it.id
            pointData.favorite = true
        }

        // 追加したマーカーをリポジトリーに登録する
        mapRepository.addFavoritePoint(pointData)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // MapController用Overrideメソッド
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * ステータス
     */
    override fun onStatus(status: MapController.Companion.MAP_STATUS) {
        mapRepository.mapStatusString.postValue("" + status)
    }

    // --------------------------------------------------------
    /**
     * 地図側から現在位置の要求
     */
    override fun onRequestCurrent() {
        dataRepository.loadFavoriteLocation()
        if (mapRepository.getLocationCurrent() == null)return
        mapCtl?.onCurrent(mapRepository.getLocationCurrent()!!)
    }

    /**
     * 最終的に描画された現在位置
     */
    override fun onLastCurrent(latLng: LatLng, point: Point) {
        // 最終的な現在地描画
        debugLineView?.let {
            it.setPoint(point)
            //Log.i(MapController.TAG + " VIEW_POSITION","latlng : " + latLng)
            //mapCtl?.onPointToMapLocation(point)?.let {
                // 取得したポイントを再度緯度経度に変換し直す
                //Log.i(MapController.TAG + " VIEW_POSITION","change latlng : " + it)

            //}
        }
        characterView?.let {
            it.setPoint(point)
        }
    }

    override fun onChangeCurrentTest(point: Point) {
        // 最終的に渡された位置情報
        debugLineView?.let {
            //it.setPoint(point)
            //Log.i(MapController.TAG + " VIEW_POSITION","latlng : " + latLng)

        }
    }


    // --------------------------------------------------------

    override fun onLongClickMap(pointData: MapLocation) {
        val address = Constant.getAddress(context!!, pointData.point.latitude, pointData.point.longitude)
        pointData.address = address

        Log.i(TAG, "setSelectLocation()")
        mapRepository.selectPoint(pointData)
    }

    override fun onMarkerClickMap(
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

    override fun onPoiClickMap(point: MapLocation) {
        setSelectLocation(point)
    }

    // --------------------------------------------------------
    override fun onMoveStartMap(isTouch: Boolean) {}
    override fun onMoveMap() {}
    override fun onMoveEndMap(isTouch: Boolean) {}

    // --------------------------------------------------------
    override fun onChangeAngle(angle: Float) {
        mapRepository.compassAngle = angle
        mapRepository.angle.postValue(-(angle))
    }

    override fun onChangeZoom(zoom: Float) {
        mapRepository.mapZoom.postValue(zoom)
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    var imageMap = mutableMapOf<Int, Bitmap?>()
    fun getImage(resource_id: Int): Bitmap? {
        if(imageMap.containsKey(resource_id)){
            var bitmap = imageMap.get(resource_id)

            if(bitmap != null) {
                return bitmap
            }
        }

        val r: Resources = mapRepository.context.getResources()
        var bitmap = BitmapFactory.decodeResource(r, resource_id)

        imageMap.set(resource_id, bitmap)

        return bitmap
    }

    fun getCurrentLocation(): LatLng? {
        return mapRepository.getLocationCurrent()
    }
}