package net.mikemobile.navi.repository.callback

import net.mikemobile.navi.data.map.MapRoute

interface RouteRepositoryCallback {
    fun onResultRoute(routeData: MapRoute)
}