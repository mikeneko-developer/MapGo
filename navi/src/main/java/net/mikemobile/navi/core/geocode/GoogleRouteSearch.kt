package net.mikemobile.navi.core.geocode

import android.content.Context
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import net.mikemobile.navi.core.gps.MyAccess
import net.mikemobile.navi.data.map.MapCheckPoint
import net.mikemobile.navi.data.map.MapLocation
import net.mikemobile.navi.data.map.MapRoute
import net.mikemobile.navi.data.old.*

/**
 *
 */
class GoogleRouteSearch(val context: Context, val listener: onSearchRouteListener):
    SearchRouteListener {
    companion object {
        enum class ROUTE_MODE(mode: String) {
            WORKING("walking"),
            DRIVING("driving")
        }
    }
    override fun startSearch(mode: ROUTE_MODE, points: ArrayList<MapLocation>) {
        selectMode = mode
        addCount = 0
        searchRoute(points)
    }

    var selectMode: ROUTE_MODE = ROUTE_MODE.WORKING
    val searchList = ArrayList<LatLng>()
    val searchRoutesData = MapRoute()

    private fun searchRoute(points: ArrayList<MapLocation>) {
        if(points.size == 0){
            Toast.makeText(context,"目的地/経由地が選択されていません", Toast.LENGTH_SHORT).show()
            return
        }

        val start = LatLng(points[0].point.latitude, points[0].point.longitude)

        // リストの初期化
        searchList.clear()
        searchRoutesData.clear()

        // スタート位置を追加
        searchRoutesData.startPoint = Location(start.latitude, start.longitude)

        // 目的地・経由地を追加
        points.forEach {
            searchList.add(it.point)
        }

        searchPointCheck(0)
    }

    var addCount = 0
    private fun searchPointCheck(count: Int) {

        // データがないのでからデータを返す
        if (searchList.size - 1 <= count) {
            // 通信終了
                // 目的地地点を追加する
            searchRoutesData.routeList.add(
                MapCheckPoint(
                        addCount,
                    searchRoutesData.routeList[searchRoutesData.routeList.size - 1].distance,
                    searchRoutesData.routeList[searchRoutesData.routeList.size - 1].duration,
                    searchRoutesData.routeList[searchRoutesData.routeList.size - 1].end_location,
                    "目的地付近です",
                    null,
                    searchRoutesData.routeList[searchRoutesData.routeList.size - 1].end_location,
                    searchRoutesData.routeList[searchRoutesData.routeList.size - 1].travel_mode
                )
            )
            addCount++

            listener.onSearchRoute(searchRoutesData)
            return
        }

        val start = searchList[count]
        val goal = searchList[count+1]

        searchRoute(start, goal, count+1)
    }

    fun searchRoute(start: LatLng, goal: LatLng, count: Int = 0) {

        if(start == null){
            Toast.makeText(context,"現在地点を取得できていません", Toast.LENGTH_SHORT).show()
            return
        }else if(goal == null){
            Toast.makeText(context,"目的地が選択されていません", Toast.LENGTH_SHORT).show()
            return
        }else  {
            Toast.makeText(context,"通信を開始します", Toast.LENGTH_SHORT).show()
        }

        var key = "AIzaSyB6p9IbfyI9F_3A8FxCJpf6vtvrOVT0qqg"
        var access = MyAccess(context!!)
        access.setOnMyNetworkListener(object: MyAccess.MyNetworkListener {
            override fun AccessResult(result: MyAccess.MyWebResponse?, e: java.lang.Exception?) {

                parseData(result?.body!!, count)
            }
        })

        access.setAccsessType(MyAccess.GET)
        access.setURL("https://maps.googleapis.com/maps/api/directions/json")
        //access.setParam("origin","%E6%9D%B1%E4%BA%AC%E9%A7%85")
        //access.setParam("destination","%E6%B5%85%E8%8D%89%E9%A7%85")
        access.setParam("origin","" + start.latitude + "," + start.longitude)
        access.setParam("destination","" + goal.latitude + "," + goal.longitude)

        if (false) {
            var waypoints = "["

            for(i in 0 until 10) {
                waypoints += "{location:"+"" + start.latitude + "," + start.longitude+"}"
            }

            waypoints += "]"
            access.setParam("waypoints", "")
        }


        access.setParam("units","metric")
        access.setParam("language","ja")
        access.setParam("region","ja")
        if (selectMode == ROUTE_MODE.DRIVING) {
            access.setParam("mode","driving")
        } else {
            access.setParam("mode","walking")
        }
        access.setParam("departure_time","now")
        access.setParam("key",key)

        access.access()
    }


    private fun parseData(body: String, count: Int) {
        val routeData = RouteData().parseData(body)
        val list = arrayListOf<StepItem>()

        var startPoint: Location? = null

        routeData.routes.forEach{
            if (startPoint == null){
                startPoint = it.legs[0].end_location
            }
            it.legs.forEach{ legItem ->
                legItem.steps.forEach{stepItem ->
                    list.add(stepItem)
                }
            }
        }

        list.clear()
        list.addAll(routeData.getRouteList())


        val polylineOp = routeData.getRoutePolyLine()
        if(polylineOp == null){
            Toast.makeText(context,"ルート情報を取得できませんでした", Toast.LENGTH_SHORT).show()
        }else {
            //Toast.makeText(context,"データを取得しました", Toast.LENGTH_SHORT).show()
            //polylineOptions.postValue(polylineOp)

        }

        setRouteData(startPoint, polylineOp, list, count)

    }

    private fun setRouteData(start: Location?, polylineOptions: PolylineOptions?, list: ArrayList<StepItem>, count: Int) {

        val end = list[list.size - 1].end_location
        if (searchRoutesData.startPoint == null) {
            searchRoutesData.startPoint = start
        }
        searchRoutesData.goalPoint = end

        if(searchRoutesData.polylineOptions == null) {
            searchRoutesData.polylineOptions = polylineOptions
        } else {
            polylineOptions?.points?.let { searchRoutesData.polylineOptions?.addAll(it) }
        }

        for(stepitem in list) {

            if (searchRoutesData.routeList.size == 0) {
                searchRoutesData.routeList.add(MapCheckPoint(
                    addCount,
                    stepitem.distance,
                    stepitem.duration,
                    stepitem.end_location,
                    stepitem.html_instructions,
                    stepitem.polyline,
                    stepitem.end_location,
                    stepitem.travel_mode
                ))
                addCount++
            } else if(searchRoutesData.routeList[searchRoutesData.routeList.size - 1].html_instructions != stepitem.html_instructions) {
                searchRoutesData.routeList.add(MapCheckPoint(
                    addCount,
                    stepitem.distance,
                    stepitem.duration,
                    stepitem.end_location,
                    stepitem.html_instructions,
                    stepitem.polyline,
                    stepitem.end_location,
                    stepitem.travel_mode
                ))
                addCount++
            }

        }

        //searchRoutesData.routeList.addAll(list)

        searchPointCheck(count)
    }

}