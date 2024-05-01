package com.sp.app.maplib.repository

import android.content.Context
import android.location.Location
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Log
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.sp.app.maplib.util.MyDate
import com.sp.app.maplib.gps.onMyLocationListener
import com.sp.app.maplib.data.TwoPoint
import com.sp.app.maplib.data.MapLocation
import com.sp.app.maplib.listener.MapRepositoryCallback
import com.sp.app.maplib.listener.OnMapListener
import com.sp.app.maplib.util.getLocateFloat

class MapRepository(var context: Context): onMyLocationListener {
    var activityLifecycle = MutableLiveData<String>().apply{value = "NONE"}

    var windowWidth: Int = 0
    var windowHeight: Int = 0

    var first_location = false // 最初だけログのGPS情報を取得する

    var this_angle: Float = 0f
    var compassAngle: Float = 0f

    var pictureInPicture = MutableLiveData<Boolean>().apply{value = false}

    ///////////////////////////////////////////////////////////////////////////
    // 位置情報用LiveData
    var isMapLocationEnable = false

    // 現在地点(GPS、NETWORK、LOG全てここに集約させる)
    var currentLocation = MutableLiveData<Location?>().apply{value = null}
    var currentDirection = MutableLiveData<Float>().apply { value = 0f }
    var currentOrientation = MutableLiveData<Float>().apply { value = 0f }



    // 対象となる2点
    var twoPointLiveData  = MutableLiveData<TwoPoint?>().apply{value = null}



    ///////////////////////////////////////////////////////////////////////////
    // GoogleMapに適用させるためのデータ
    var currentPosition: LatLng? = null
    val currentPositionLiveData  = MutableLiveData<LatLng?>().apply{value = null}
    fun setLocationCurrent(current: LatLng) {
        currentPosition = current
        currentPositionLiveData.postValue(current)
        lat.postValue(current.latitude)
        lng.postValue(current.longitude)
    }
    fun getLocationCurrent(): LatLng? {
        return currentPosition
    }

    ///////////////////////////////////////////////////////////////////////////
    var lat = MutableLiveData<Double>().apply{value = 0.0}
    var lng = MutableLiveData<Double>().apply{value = 0.0}
    var angle = MutableLiveData<Float>().apply{value = 0f}
    var mapZoom = MutableLiveData<Float>().apply{value = 17.5f}

    var mapGps = MutableLiveData<String>().apply{value = ""}
    var thisGps = MutableLiveData<String>().apply{value = ""}
    var thisNetwork = MutableLiveData<String>().apply{value = ""}
    var logGps = MutableLiveData<String>().apply{value = ""}
    var logNetwork = MutableLiveData<String>().apply{value = ""}

    var logText = MutableLiveData<Spanned>().apply{
        value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml("", FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml("")
        }
    }

    var deviceOrientation = MutableLiveData<Float>().apply{value = 0f}
    var magneticDirection = MutableLiveData<Float>().apply{value = 0f}

    ///////////////////////////////////////////////////////////////////////////

    var angleMode = MutableLiveData<Int>(0)

    val mapStatusString = MutableLiveData<String>().apply{
        value = "MAP_LOAD"
    }


    var callback = mutableMapOf<String, MapRepositoryCallback>()
    fun addMapCallback(tag: String, listener: MapRepositoryCallback){
        if(!callback.containsKey(tag) || callback.get(tag) == null){
            callback.put(tag, listener)
        }
    }

    fun removeMapCallback(tag: String) {
        if(callback.containsKey(tag)){
            callback.remove(tag)
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    fun onClickMarker(type: Int, marker: Marker?){
        for(key in callback.keys){
            callback.get(key)?.onMarkerClick(type, marker)
        }
    }

    fun onClickSelectPoint(type: Int, markerId: String) {
        for(key in callback.keys){
            callback.get(key)?.onClickSelectPoint(type, markerId)
        }
    }


    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////

    fun startLocation(){
        //location.setOnMyLocationListener(locationListener)
        first_location = true
        //location.start()
    }

    fun stopLocation(){
        //location.setOnMyLocationListener(null)
        //location.stop()
    }

    ///////////////////////////////////////////////////////////////////////////

    var log_text_list = mutableListOf<String>()
    fun setLocationTextLog(log_text: String, color: String){
        log_text_list.add(0, "<p><font color=\""+color+"\">" + log_text + "</font></p>")

        if(log_text_list.size >= 100){
            log_text_list.removeAt(log_text_list.size - 1)
        }

        var view_log_text = "<html>"
        for(text in log_text_list){
            view_log_text += text
        }
        view_log_text += "</html>"

        val res:Spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(view_log_text, FROM_HTML_MODE_COMPACT)
        } else {
            Html.fromHtml(view_log_text)
        }

        logText.postValue(res)
    }


    val ORIENTATION_THRESHOLD = 30f // 位置情報からの方角を更新して良いと判断する端末の方位情報を起点とする閾値

    override fun onCurrentLocation(location: Location, direction: Float) {
        currentLocation.postValue(location)
        currentDirection.postValue(direction)

        // 現在地をLatLngに変換して渡す
        val current = LatLng(location.latitude, location.longitude)
        setLocationCurrent(current)
    }

    override fun onGpsLocation(location: Location?) {
        gpsLocation.postValue(location)
        location?.let {
            thisGps.postValue("GPS" +
                    "\n緯度" + getLocateFloat(it.latitude) + "" +
                    " 経度:" + getLocateFloat(it.longitude) + "" +
                    "\n時間:" + MyDate.getTimeString(it.time)
            )

            setLocationTextLog(MyDate.TimeString(it.time) + " GPS　　:" + "" +
                    "緯度" + getLocateFloat(it.latitude) + " 経度:" + getLocateFloat(it.longitude) + "","#FF8E3B")

        }
    }

    override fun onNetworkLocation(location: Location?) {
        networkLocation.postValue(location)
        location?.let {
            thisNetwork.postValue("基地局" +
                    "\n緯度" + getLocateFloat(it.latitude) + "" +
                    " 経度:" + getLocateFloat(it.longitude) + "" +
                    "\n時間:" + MyDate.getTimeString(it.time)
            )

            setLocationTextLog(MyDate.TimeString(it.time) + " NET　　:" + "" +
                    "緯度" + getLocateFloat(it.latitude) + " 経度:" + getLocateFloat(it.longitude) + "","#FA4AE5")
        }
    }

    override fun onGpsLogLocation(location: Location?) {
        gpsLogLocation.postValue(location)
//        location?.let {
//            logGps.postValue("ログGPS" +
//                    "\n緯度" + getLocateFloat(it.latitude) + "" +
//                    " 経度:" + getLocateFloat(it.longitude) + "" +
//                    "\n時間:" + MyDate.getTimeString(it.time)
//            )
//
//
//            setLocationTextLog(MyDate.TimeString(it.time) + " GPSLOG:" + "" +
//                    "緯度" + getLocateFloat(it.latitude) + " 経度:" + getLocateFloat(it.longitude) + "","#FFEB3B")
//        }
    }

    override fun onNetworkLogLocation(location: Location?) {
        networkLogLocation.postValue(location)

        location?.let {
            logNetwork.postValue("ログ基地局" +
                "\n緯度" + getLocateFloat(it.latitude) + "" +
                " 経度:" + getLocateFloat(it.longitude) + "" +
                "\n時間:" + MyDate.getTimeString(it.time)
            )


            setLocationTextLog(MyDate.TimeString(it.time) + " NETLOG:" + "" +
                "緯度" + getLocateFloat(it.latitude) + " 経度:" + getLocateFloat(it.longitude) + "","#3BFFE8")
        }

    }

    ///////////////////////////////////////////////////////////////////////////



    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 2021/12/18　コード整理
    ////////////////////////////////////////////////////////////////////////////////////////////////

    var mapCallback: OnMapListener? = null
    fun setOnMapListener(l: OnMapListener?, className: String? = null) {
        var update = true

        if (l == null && className != null) {
            // リスナーの削除かつクラス名がある場合、同名のクラス名だったら削除する（他でも使用するため
            update = false
            mapCallback?.let {
                it.javaClass::class.simpleName?.let {
                    if (it == className) {
                        update = true
                    }
                }
            }
        }

        if (update)mapCallback = l
    }

    // 指定する中心位置へ移動する
    var selectPoints = MutableLiveData<MapLocation?>().apply{value = null}
    fun setSelectPoint(pointData: MapLocation) {
        selectPoints.postValue(pointData)
    }


    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////
    // 地図で長押しされたポイントを各ViewModelで受け取るための変数
    var selectLongTapPoint = MutableLiveData<MapLocation?>().apply{ value = null }
    fun selectPoint(pointData: MapLocation) {
        selectLongTapPoint.postValue(pointData)
        mapCallback?.onSelectMapPoint(pointData)
    }


    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////
    // 地図上で選択されたポイントを保存する変数
    var selectPointMarkersLiveData = MutableLiveData<ArrayList<MapLocation>?>().apply{ value = null }
    var selectPointMarkers = ArrayList<MapLocation>()
    fun getSelectPointList(): ArrayList<MapLocation> {
        return selectPointMarkers
    }

    fun getRouteList(): ArrayList<MapLocation> {
        return selectPointMarkers.clone() as ArrayList<MapLocation>
    }

    fun getRouteData(): ArrayList<MapLocation> {
        if (currentPosition == null) return ArrayList<MapLocation>()

        var list = selectPointMarkers.clone() as ArrayList<MapLocation>
        val startPoint = MapLocation("スタート地点", currentPosition!!)
        list.add(0, startPoint)
        // スタート地点のマーカーを追加する
        //mapCallback?.onAddMarker(startPoint)

        return list
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////
    private fun getMarkerPosition(pointData: MapLocation): Int {

        for(i in 0 until selectPointMarkers.size) {
            if (pointData.address == selectPointMarkers[i].address ||
                pointData.point.latitude == selectPointMarkers[i].point.latitude ||
                pointData.point.longitude == selectPointMarkers[i].point.longitude) {
                return i
            }
        }
        return -1
    }

    fun setMovePointMarker(fromPos: Int, toPos: Int) {
        val pointData1 = selectPointMarkers[fromPos]
        val pointData2 = selectPointMarkers[toPos]

        if (pointData2.haveGoal) {
            //pointData1.haveGoal = true
            //pointData2.haveGoal = false
        } else if(pointData1.haveGoal){
            //pointData2.haveGoal = true
            //pointData1.haveGoal = false

        }

        selectPointMarkers[fromPos] = pointData2
        selectPointMarkers[toPos] = pointData1


        mapCallback?.onChangeMarker(selectPointMarkers[fromPos])
        mapCallback?.onChangeMarker(selectPointMarkers[toPos])

        selectPointMarkersLiveData.postValue(selectPointMarkers)
    }


    /**
     * 引数のポイントを地図上でマーカー表示する
     */
    fun setSelectPointMaker(pointData: MapLocation): Int {
        var prevGoal:MapLocation? = null
        var position = -1
        if (pointData.newPosition) {
            pointData.newPosition = false

            if (pointData.haveGoal) {
                if (selectPointMarkers.size > 0){
                    prevGoal = selectPointMarkers[selectPointMarkers.size - 1]
                    prevGoal.haveGoal = false
                }

                selectPointMarkers.add(pointData)
                position = selectPointMarkers.size - 1
            } else {
                if (selectPointMarkers.size > 0) {
                    position = selectPointMarkers.size - 1
                    selectPointMarkers.add(position, pointData)
                } else {
                    selectPointMarkers.add(pointData)
                    position = selectPointMarkers.size - 1
                }
            }

            // マーカーをつける
            mapCallback?.onAddMarker(pointData)
            prevGoal?.let { mapCallback?.onChangeMarker(it) }
        } else {
            if (pointData.haveGoal) {
                if (selectPointMarkers.size > 1){
                    prevGoal = selectPointMarkers[selectPointMarkers.size - 1]
                    prevGoal.haveGoal = false
                }

                val position = getMarkerPosition(pointData)
                selectPointMarkers.removeAt(position)
                selectPointMarkers.add(pointData)
            } else {
                val position = getMarkerPosition(pointData)
                selectPointMarkers.set(position, pointData)

                val nextGoalPosi = selectPointMarkers.size - 2
                prevGoal = selectPointMarkers[nextGoalPosi]
                prevGoal.haveGoal = true

                selectPointMarkers.removeAt(nextGoalPosi)
                selectPointMarkers.add(prevGoal)

            }

            mapCallback?.onChangeMarker(pointData)
            prevGoal?.let { mapCallback?.onChangeMarker(it) }
        }

        selectPointMarkersLiveData.postValue(selectPointMarkers)
        return position
    }

    fun updateSelectPoint(pointData: MapLocation, add: Boolean) {
        for(i in 0 until selectPointMarkers.size) {
            val point = selectPointMarkers[i]
            if (point.address == pointData.address &&
                point.point.latitude == pointData.point.latitude &&
                point.point.longitude == pointData.point.longitude) {
                selectPointMarkers[i] = pointData
                break
            }
        }
        selectPointMarkersLiveData.postValue(selectPointMarkers)
    }


    private fun getPosition(checkPoint: MapLocation): Int {
        for(i in 0 until selectPointMarkers.size) {
            if (checkPoint.address == selectPointMarkers[i].address ||
                checkPoint.point.latitude == selectPointMarkers[i].point.latitude ||
                checkPoint.point.longitude == selectPointMarkers[i].point.longitude) {
                return i
            }
        }
        return -1
    }

    /**
     * 指定したポイントのマーカーを地図上から削除する
     */
    fun setDeleteSelectPointMarker(pointData: MapLocation) {
        val point = getPosition(pointData)
        if(point >= 0) {
            selectPointMarkers.removeAt(point)
        }

        var lastPoint: MapLocation? = null
        if (selectPointMarkers.size > 0) {
            val point = selectPointMarkers[selectPointMarkers.size - 1]

            if (!point.haveGoal) {
                point.haveGoal = true
                lastPoint = point
                selectPointMarkers[selectPointMarkers.size - 1] = lastPoint
            } else {
                lastPoint = point
            }
        }

        if (selectPointMarkers.size == 0) {
            selectPointMarkersLiveData.postValue(null)
        } else {
            selectPointMarkersLiveData.postValue(selectPointMarkers)
        }

        pointData.delete = 1
        mapCallback?.onRemoveMarker(pointData)
        lastPoint?.let { mapCallback?.onChangeMarker(it) }
    }

    /**
     * 登録された選択マーカーを全て削除する（Map以外から呼び出される）
     */
    fun clearSelectPoint() {
        try {
            val list = selectPointMarkers.clone() as ArrayList<MapLocation>
            for(i in 0 until list.size) {
                setDeleteSelectPointMarker(list[i])
            }
        } catch(e: Exception) {
            Log.e("MapRepository", "" + e.toString())
        }

        // ルート線に関する情報をクリアする
        selectLongTapPoint.postValue(null)

        selectPointMarkers = ArrayList<MapLocation>()
        selectPointMarkersLiveData.postValue(null)
    }

    fun clearStartPoint() {
        for(location in selectPointMarkers) {
            if (location.name == "スタート地点") {
                setDeleteSelectPointMarker(location)
                break
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////
    // ■■■■■ ログ表示用 ■■■■■■■■■
    // GPSの現在地点
    var gpsLocation = MutableLiveData<Location?>().apply{value = null}

    // 基地局の現在地点
    var networkLocation = MutableLiveData<Location?>().apply{value = null}

    // GPSログの最終地点
    var gpsLogLocation = MutableLiveData<Location?>().apply{value = null}

    // GPSログの最終地点
    var networkLogLocation = MutableLiveData<Location?>().apply{value = null}

}