package net.mikemobile.navi.core.gps

import android.content.Context
import android.location.Location
import net.mikemobile.navi.core.gps.onMyLocationListener


class SimpleLocationListener : onMyLocationListener {

    override fun onCurrentLocation(context: Context?, location: Location) {}
    override fun onGpsLocation(context: Context?, location: Location?) {}
    override fun onNetworkLocation(context: Context?, location: Location?) {}
    override fun onNetworkLogLocation(context: Context?, location: Location?) {}
    override fun onGpsLogLocation(context: Context?, location: Location?) {}
}