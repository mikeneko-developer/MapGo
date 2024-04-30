package net.mikemobile.navi.database

import net.mikemobile.sampletimer.database.entity.LocationFavorite
import net.mikemobile.sampletimer.database.entity.Robo

interface OnRoomDatabaseListener {
    fun onCreated(data: LocationFavorite)
    fun onUpdated(data: LocationFavorite)
    fun onError(error_code: Int)
    fun onDeleted()
    fun onRead(list: List<LocationFavorite>)


    fun onCreated(data: Robo)
    fun onUpdated(data: Robo)
    fun onReadRobo(list: List<Robo>)


}