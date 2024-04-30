package com.sp.app.maplib.gps

import android.content.Context
import android.location.Location
import com.sp.app.maplib.gps.onMyLocationListener


class SimpleLocationListener : onMyLocationListener {
    override fun onCurrentLocation(location: Location, direction: Float) {}
    override fun onGpsLocation(location: Location?) {}
    override fun onNetworkLocation(location: Location?) {}
    override fun onNetworkLogLocation(location: Location?) {}
    override fun onGpsLogLocation(location: Location?) {}
}