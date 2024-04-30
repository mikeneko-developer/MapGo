package com.sp.app.maplib.listener

import com.sp.app.maplib.data.MapLocation


interface OnMapListener {

    fun onSelectMapPoint(pointData: MapLocation)

    fun onAddMarker(pointData: MapLocation)
    fun onChangeMarker(pointData: MapLocation)
    fun onRemoveMarker(pointData: MapLocation)
    fun onAddFavoriteMarker(pointData: MapLocation)

}