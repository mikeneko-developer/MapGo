package net.mikemobile.navi.core.gps

import android.content.Context
import android.location.Location

interface onMyLocationListener {
    fun onCurrentLocation(
        context: Context?,
        location: Location
    )
    fun onGpsLocation(
        context: Context?,
        location: Location?
    )

    fun onNetworkLocation(
        context: Context?,
        location: Location?
    )

    fun onNetworkLogLocation(
        context: Context?,
        location: Location?
    )

    fun onGpsLogLocation(
        context: Context?,
        location: Location?
    )
}