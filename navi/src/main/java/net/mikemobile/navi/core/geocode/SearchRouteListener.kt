package net.mikemobile.navi.core.geocode

import net.mikemobile.navi.data.map.MapLocation
import net.mikemobile.navi.data.map.MapRoute

interface SearchRouteListener {
    fun startSearch(mode: GoogleRouteSearch.Companion.ROUTE_MODE, points: ArrayList<MapLocation>)

}

interface onSearchRouteListener {
    fun onSearchRoute(routesData: MapRoute)
}