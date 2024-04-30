package com.sp.app.maplib.data
import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

data class MapLocation(
    var name: String,
    var point: LatLng,
    var haveGoal:Boolean = false,
    var delete:Int = 0,
    var marker_id: String? = null,
    var favorite: Boolean = false,
    var newPosition: Boolean = false,
    var pointType:Int = 0,
    var address: String = ""


) : Serializable