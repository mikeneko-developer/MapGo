package com.sp.app.maplib.ui

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.*
import com.sp.app.maplib.repository.MapRepository


class RouteViewModel(
    private val context: Context,
    val mapRepository: MapRepository,
) : ViewModel() {

    companion object {
        const val TAG = "RouteViewModel"
    }

    ///////////////////////////////////////////////////

    var nextPointDistance = MutableLiveData<Float>().apply {
        value = -1f
    }
    ///////////////////////////////////////////////////
    fun initialize() {

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////
    var nextMarker: Marker? = null
//    fun setNextLocateMaker(latLng: LatLng?){
//        if(latLng == null){
//            if(nextMarker != null){
//                mapCtl?.onDeleteMarker(nextMarker!!.id)
//                nextMarker!!.remove()
//                nextMarker = null
//            }
//
//            if(nextPoliLine != null) {
//                mapCtl?.onRemovePolyline(nextPoliLine!!.id)
//                nextPoliLine!!.remove()
//                nextPoliLine = null
//            }
//        }else {
//            // 現在位置
//
//            if (nextMarker == null) {
//                val options = createMarkerOptions("次のポイント", latLng, com.sp.app.maplib.R.drawable.ic_next)
//                nextMarker = mapCtl?.onAddMarker(options)
//            } else {
//                nextMarker!!.position = latLng
//            }
//
//            mapCtl?.getCurrent()?.let {
//                setDistancePolyline(it, latLng)
//            }
//
//            mapCtl?.let {
//                val point = it.onLatLogToScreenPosition(latLng)
//
//                Log.i(MapViewModel.TAG, "nextPoint point:" + point)
//
//            }
//        }
//    }

//    var nextPoliLine:Polyline? = null
//    fun setDistancePolyline(current: LatLng, next: LatLng) {
//        nextPoliLine?.let {
//            it.remove()
//        }
//
//        val polyOptions = PolylineOptions()
//        polyOptions.add(current)
//        polyOptions.add(next)
//        polyOptions.color(Color.RED)
//        polyOptions.width(3f)
//        polyOptions.geodesic(true) //true:大圏コース　false:直線
//
//        nextPoliLine = mapCtl?.onAddPolyline(polyOptions)
//
//        val distance = distanceCalc(current.latitude,current.longitude,next.latitude, next.longitude)
//
//    }
//
//    ////////////////////////////////////////////////////////////////////////////////////////////////
//    //
//    ////////////////////////////////////////////////////////////////////////////////////////////////
//    var polylineId: String? = null
//    fun setPolyLine(polylineOptions: PolylineOptions?){
//
//        if(polylineOptions == null){
//            polylineId?.let {
//                mapCtl?.onRemovePolyline(it)
//            }
//
//        }else {
//            val polyline = mapCtl?.onAddPolyline(polylineOptions)
//            polyline?.let {
//
//                if(polylineId != null) {
//                    mapCtl?.onRemovePolyline(polylineId!!)
//                }
//
//                polylineId = it.id
//            }
//        }
//    }

    /**
     * 選択マーカーを追加する処理
     * @param pointData
     */
//    override fun onAddMarker(pointData: MapLocation) {
//        Log.i(MapViewModel.TAG + " ADD_MAKER", "onAddMaker point name:" + pointData.name)
//
//        val list = mapRepository.getSelectPointList()
//        if (pointData.haveGoal && list.size > 0) {
//            // 目的地に設定するので現在のゴールを変更する
//        }
//
//        val resourceId: Int = if (pointData.haveGoal) {
//            com.sp.app.maplib.R.drawable.ic_goal
//        } else if (pointData.name == "スタート地点") {
//            com.sp.app.maplib.R.drawable.ic_start
//        } else {
//            com.sp.app.maplib.R.drawable.ic_via
//        }
//        val options =
//            Constant.createMarkerOptions(context, pointData.name, pointData.point, resourceId)
//
//
//        val maker = mapCtl?.onAddMarker(options, MapViewModel.MARKER_SELECT)
//        maker?.let {
//            pointData.marker_id = it.id
//        }
//
//        // 追加したマーカーをリポジトリーに登録する
//        if (pointData.name != "スタート地点") {
//            //mapRepository.addSelectPoint(pointData)
//
//            mapRepository.updateSelectPoint(pointData, true)
//        }
//
//
//        mapCtl?.let {
//            val point = it.onLatLogToScreenPosition(pointData.point)
//            Log.i(MapViewModel.TAG, "ルート地点マーカー point:" + point)
//
//        }
//    }
//
//    /**
//     * 選択マーカーの状態を変更する処理
//     */
//    override fun onChangeMarker(pointData: MapLocation) {
//        // 先にゴール指定のデータのアイコンを経由地に変更する
//        val list = mapRepository.getSelectPointList()
//
//        if(pointData.haveGoal) {
//            //現在の目的地データ
//            pointData.marker_id?.let {
//                mapCtl?.onChangeMarkerIcon(it, com.sp.app.maplib.R.drawable.ic_goal) }
//
//
//        } else {
//            // ゴールから経由地変更
//
//            pointData.marker_id?.let {
//                mapCtl?.onChangeMarkerIcon(it, com.sp.app.maplib.R.drawable.ic_via) }
//
//        }
//        mapRepository.updateSelectPoint(pointData, false)
//    }
//
//    /**
//     * 選択マーカーを削除する処理
//     * @param pointData
//     */
//    override fun onRemoveMarker(pointData: MapLocation) {
//        var res = true
//        if (pointData.marker_id == null || mapCtl == null) {
//            // marker_idがない場合、マップに追加されていないため、削除したことにして返す
//        } else {
//            res = mapCtl!!.onDeleteMarker(pointData.marker_id!!)
//        }
//
//        // 最後の
//        val list = mapRepository.getSelectPointList()
//
//        // 現在の位置を取得
//        var p1 = -1
//        for(i in 0 until list.size) {
//            if (list[i].marker_id == pointData.marker_id) {
//                p1 = i
//                break
//            }
//        }
//
//        if (list.size > 1) {
//            // 最終的なゴールの位置を取得
//            var goalPoint: MapLocation
//            if (p1 == list.size - 1) {
//                goalPoint = list[list.size - 2]
//            } else {
//                goalPoint = list[list.size - 1]
//            }
//
//            goalPoint.marker_id?.let { mapCtl?.onChangeMarkerIcon(it, com.sp.app.maplib.R.drawable.ic_goal) }
//        }
//
//        //mapRepository.deleteSelectPoint(res, pointData)
//        //mapRepository.updateSelectPoint(pointData)
//    }

//    fun onAddFavoriteMarker(pointData: MapLocation) {
//        Log.i(MapViewModel.TAG + " ADD_MAKER", "onAddMaker point name:" + pointData.name)
//
//        val options = Constant.createMarkerOptions(
//            context,
//            pointData.name,
//            pointData.point,
//            R.drawable.ic_favorite
//        )
//        val maker = mapCtl?.onAddMarker(options, MapViewModel.MARKER_FAVORITE)
//        maker?.let {
//            pointData.marker_id = it.id
//            pointData.favorite = true
//        }
//
//        // 追加したマーカーをリポジトリーに登録する
//        //mapRepository.addFavoritePoint(pointData)
//    }
}