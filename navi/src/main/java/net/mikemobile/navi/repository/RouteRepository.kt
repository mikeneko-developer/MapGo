package net.mikemobile.navi.repository

import android.content.Context
import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import net.mikemobile.navi.data.map.MapLocation
import net.mikemobile.navi.data.map.MapRoute
import net.mikemobile.navi.data.old.StepItem
import net.mikemobile.navi.repository.callback.RouteRepositoryCallback
import net.mikemobile.navi.core.geocode.GoogleRouteSearch
import net.mikemobile.navi.core.geocode.SearchRouteListener
import net.mikemobile.navi.core.geocode.onSearchRouteListener
import net.mikemobile.navi.data.map.MapCheckPoint
import net.mikemobile.navi.util.distanceCalc
import net.mikemobile.navi.util.getDistance

class RouteRepository(var context: Context) {
    // 選択した地点をまとめた
    var routesData = MutableLiveData<MapRoute?>().apply{
        value = null
    }

    ///////////////////////////////////////////////////////////////////////////
    var currentPosition = LatLng(0.0,0.0)
    val currentPositionLiveData  = MutableLiveData<LatLng?>().apply{value = null}
    fun setLocationCurrent(current: LatLng) {
        currentPosition = current
        currentPositionLiveData.postValue(current)
    }

    fun calcNextPoint(nextPoint: LatLng): Int {

        val distance = distanceCalc(
            currentPosition.latitude, currentPosition.longitude,
            nextPoint.latitude, nextPoint.longitude
        )

        return distance
    }
    ///////////////////////////////////////////////////////////////////////////

    var callback = mutableMapOf<String, RouteRepositoryCallback>()
    fun addMapCallback(tag: String, listener: RouteRepositoryCallback){
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
    fun searchRoute(mode: GoogleRouteSearch.Companion.ROUTE_MODE, points: ArrayList<MapLocation>) {
        // ルート検索を実施するので、現在スタート地点となっている部分を

        val routeSearch: SearchRouteListener = GoogleRouteSearch(context, object:
            onSearchRouteListener {
            override fun onSearchRoute(searchRoutesData: MapRoute) {
                routesData.postValue(searchRoutesData)

                for(key in callback.keys){
                    callback.get(key)?.onResultRoute(searchRoutesData)
                }
            }
        })
        routeSearch.startSearch(mode, points)
    }


    ///////////////////////////////////////////////////////////////////////////
    fun getRoute():ArrayList<MapCheckPoint> {
        var route = ArrayList<MapCheckPoint>()
        routesData.value?.let {
            route = it.routeList
        }

        return route
    }

    fun getRoutes(): MapRoute {
        routesData.value?.let {
            return it
        }
        return MapRoute()
    }

    fun routeClear() {
        routesData.postValue(null)

    }


    // 指定する中心位置
    ///////////////////////////////////////////////////////////////////////////
    var focusLocation = MutableLiveData<LatLng?>().apply{value = null}
    fun setFocusLocation(latlng: LatLng) {
        focusLocation.postValue(latlng)
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // ルート案内用
    ///////////////////////////////////////////////////////////////////////////////////////////////

    // 選択位置 保持用
    var nextPointLatLng = MutableLiveData<LatLng?>().apply{value = null}
    fun setNextPoint(latLng: LatLng) {
        nextPointLatLng.postValue(latLng)
    }

    fun clearNextPoint() {
        nextPointLatLng.postValue(null)
    }

    fun setCurrentPoint(latLng: LatLng) {
        nextPointLatLng.value?.let {
            calcDistance(latLng, it)
        }

    }
    private fun calcDistance(curent: LatLng, next: LatLng) {
        var distance = getDistance(curent.latitude,curent.longitude,
            next.latitude, next.latitude)

    }

}