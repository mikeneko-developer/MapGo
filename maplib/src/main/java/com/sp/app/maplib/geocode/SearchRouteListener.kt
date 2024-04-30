package com.sp.app.maplib.ui.map.geocode

import com.sp.app.maplib.ui.map.geocode.GoogleRouteSearch
import com.sp.app.maplib.data.MapLocation
import com.sp.app.maplib.data.MapRoute


interface SearchRouteListener {
    fun startSearch(mode: GoogleRouteSearch.Companion.ROUTE_MODE, points: ArrayList<MapLocation>)

}

interface onSearchRouteListener {
    fun onSearchRoute(routesData: MapRoute)
}