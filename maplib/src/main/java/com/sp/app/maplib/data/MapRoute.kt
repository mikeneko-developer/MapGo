package com.sp.app.maplib.data

import com.google.android.gms.maps.model.PolylineOptions
import com.sp.app.maplib.ui.map.geocode.Location

/**
 * ルート情報をまとめて保存するデータクラス
 */
data class MapRoute(
    var startPoint: Location?,
    var goalPoint: Location?,
    var polylineOptions: PolylineOptions?,
    var routeList: ArrayList<MapCheckPoint>
) {
    constructor(): this(null, null, null, ArrayList<MapCheckPoint>()) {}

    fun clear() {
        startPoint = null
        goalPoint = null
        polylineOptions = null
        routeList = ArrayList<MapCheckPoint>()
    }
}