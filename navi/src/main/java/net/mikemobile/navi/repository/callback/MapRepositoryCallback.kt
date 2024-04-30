package net.mikemobile.navi.repository.callback

import com.google.android.gms.maps.model.Marker

interface MapRepositoryCallback {

    fun onMarkerClick(type: Int, marker: Marker?)
    fun onClickSelectPoint(type: Int, name: String)

}