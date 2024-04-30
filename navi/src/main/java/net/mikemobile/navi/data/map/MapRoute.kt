package net.mikemobile.navi.data.map

import com.google.android.gms.maps.model.PolylineOptions
import net.mikemobile.navi.data.old.Location
import net.mikemobile.navi.data.old.StepItem

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