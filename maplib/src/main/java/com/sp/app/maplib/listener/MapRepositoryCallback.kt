package com.sp.app.maplib.listener

import com.google.android.gms.maps.model.Marker

interface MapRepositoryCallback {

    fun onMarkerClick(type: Int, marker: Marker?)
    fun onClickSelectPoint(type: Int, name: String)

}