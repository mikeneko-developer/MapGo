package net.mikemobile.navi.data.old

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import net.mikemobile.navi.core.gps.parseJsonpOfDirectionAPI
import net.mikemobile.navi.util.MyHash
import org.json.JSONObject

class RouteData {

    var status: String? = null
    var geocoded_waypoints: GeocodedWayPoints? = null
    var routes: ArrayList<Routes> = arrayListOf<Routes>()

    private var routeLineList: List<List<HashMap<String,String>>>? = null

    fun parseData(text: String): RouteData {
        var data = MyHash.getDecode(text)

        if(true){
            status = MyHash.getString(data, "status")
        }
        if(true){
            var _geocoded_waypoints = MyHash.getArrayHash(data, "geocoded_waypoints")
            val geocoder_status = MyHash.getString(_geocoded_waypoints[0], "geocoder_status")
            val place_id = MyHash.getString(_geocoded_waypoints[0], "place_id")
            val types = MyHash.getArray(_geocoded_waypoints[0], "types") as ArrayList<String>

            if (geocoder_status != null) {
                geocoded_waypoints = GeocodedWayPoints(geocoder_status, place_id, types)
            }
        }
        if(true){
            val routeList = MyHash.getArrayHash(data, "routes")

            routeList.forEach{
                routes.add(Routes(it))
            }
        }

        if(true){
            val jObject: JSONObject
            var routes: List<List<HashMap<String, String>>>? = null

            try {
                jObject = JSONObject(text)
                val parser =
                    parseJsonpOfDirectionAPI()
                routes = parser.parse(jObject)

            } catch (e: Exception) {
                e.printStackTrace()
            }

            routes?.let{
                routeLineList = it
            }

        }


        return this
    }

    fun getRouteList(): ArrayList<StepItem> {
        Log.i("RouteData", "getRouteList()")
        val list = arrayListOf<StepItem>()

        var startPoint: Location? = null

        for(route in routes) {
            for(leg in route.legs) {
                if (startPoint == null){
                    startPoint = leg.end_location
                }

                for(step in leg.steps) {
                    Log.i("RouteData", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
                    Log.i("RouteData", "説明:" + step.html_instructions)

                    if (list.size > 0) {
                        Log.i("RouteData", "最後の説明:" + list[list.size - 1].html_instructions)
                        Log.i("RouteData", "説明が同じ:" + (list[list.size - 1].html_instructions == step.html_instructions))
                    }

                    if (list.size > 0 && list[list.size - 1].html_instructions == step.html_instructions) {
                        // 同じ説明が続くため不要
                    } else {
                        list.add(step)
                    }
                }

            }
        }

        return list
    }


    fun getRoutePolyLine(): PolylineOptions?{
        if(routeLineList == null){
            android.util.Log.i("RouteData","getRoutePolyLine is null")
            return null
        }

        var points: ArrayList<LatLng>? = null
        var lineOptions: PolylineOptions? = null

        if (routeLineList!!.size != 0) {

            for (i in routeLineList!!.indices) {
                points = ArrayList()
                lineOptions = PolylineOptions()


                val path = routeLineList!![i]


                for (j in path.indices) {
                    val point = path[j]

                    val lat = java.lang.Double.parseDouble(point["lat"]!!)
                    val lng = java.lang.Double.parseDouble(point["lng"]!!)
                    val position = LatLng(lat, lng)

                    points.add(position)
                }

                //ポリライン
                lineOptions.addAll(points)
                lineOptions.width(10f)
                lineOptions.color(0x550000ff)

            }

            //android.util.Log.i("RouteData","getRoutePolyLine " + routeLineList!!.size)

            return lineOptions
        } else {
            android.util.Log.i("RouteData","getRoutePolyLine is null")
            return null
        }
    }

}


data class GeocodedWayPoints(
    var geocoder_status:String,
    var place_id:String,
    var types:ArrayList<String>
)


data class Routes(
    var legs: ArrayList<LegItem>
){
    constructor(data: HashMap<String,Any>):this(arrayListOf<LegItem>()){
        val legList = MyHash.getArrayHash(data, "legs")
        legList.forEach{
            legs.add(LegItem(it))
        }
    }
}

data class LegItem(
    var distance: Distance?,
    var duration: Duration?,
    var end_address: String,
    var end_location: Location?,
    var start_address:String,
    var start_location: Location?,
    var steps:ArrayList<StepItem>
){
    constructor(data: HashMap<String,Any>):this(null,null,"",null,"",null,arrayListOf<StepItem>()){
        distance = Distance(data)
        duration = Duration(data)
        end_location = Location(MyHash.getHash(data, "end_location"))
        start_location = Location(MyHash.getHash(data, "start_location"))

        end_address = MyHash.getString(data, "end_address")
        start_address = MyHash.getString(data, "start_address")

        val stepList = MyHash.getArrayHash(data, "steps")
        stepList.forEach{
            steps.add(StepItem(it))
        }
    }
}

data class StepItem(
    var distance: Distance?,
    var duration: Duration?,
    var end_location: Location?,
    var html_instructions: String,
    var polyline: PolyLine?,
    var start_location: Location?,
    var travel_mode: String
){
    constructor(data: HashMap<String,Any>):this(null,null,null,"",null,null,""){

        distance = Distance(data)
        duration = Duration(data)
        end_location = Location(MyHash.getHash(data, "end_location"))
        start_location = Location(MyHash.getHash(data, "start_location"))
        polyline = PolyLine(data)

        html_instructions = MyHash.getString(data, "html_instructions")
        travel_mode = MyHash.getString(data, "travel_mode")
    }
}

data class PolyLine(var points: String){
    constructor(data: HashMap<String,Any>):this(""){
        android.util.Log.i("RouteData","polyline:" + data)
        val polyline = MyHash.getHash(data, "polyline")
        points = MyHash.getString(polyline, "points")
    }
}

data class Location(var lat:Double?,var lng:Double?){
    constructor(data: HashMap<String,Any>):this(null,null){
        lat = MyHash. getDouble(data, "lat")
        lng = MyHash.getDouble(data, "lng")
    }
}

data class Duration(var text: String, var value:Int){

    constructor(data: HashMap<String,Any>):this("",0){
        val duration = MyHash.getHash(data, "duration")
        text = MyHash.getString(duration, "text")
        value = MyHash.getInteger(duration, "value")
    }
}
data class Distance(var text: String, var value:Int){

    constructor(data: HashMap<String,Any>):this("",0){
        val distance = MyHash.getHash(data, "distance")
        text = MyHash.getString(distance, "text")
        value = MyHash.getInteger(distance, "value")
    }
}

