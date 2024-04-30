package com.sp.app.maplib.gps

import android.content.Context
import android.location.Location

interface onMyLocationListener {
    fun onCurrentLocation(
        location: Location,
        direction: Float
    )
    fun onGpsLocation(
        location: Location?
    )

    fun onNetworkLocation(
        location: Location?
    )

    fun onNetworkLogLocation(
        location: Location?
    )

    fun onGpsLogLocation(
        location: Location?
    )
}