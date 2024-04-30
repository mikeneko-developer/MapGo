package net.mikemobile.navi.ui.map

import net.mikemobile.navi.data.map.MapLocation

interface OnMapListener {
    fun onAddMarker(pointData: MapLocation)
    fun onChangeMarker(pointData: MapLocation)
    fun onRemoveMarker(pointData: MapLocation)

    fun onAddFavoriteMarker(pointData: MapLocation)

}