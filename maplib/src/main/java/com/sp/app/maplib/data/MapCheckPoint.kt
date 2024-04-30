package com.sp.app.maplib.data

import com.sp.app.maplib.ui.map.geocode.Distance
import com.sp.app.maplib.ui.map.geocode.Duration
import com.sp.app.maplib.ui.map.geocode.Location
import com.sp.app.maplib.ui.map.geocode.PolyLine

data class MapCheckPoint(
    val id: Int,
    var distance: Distance?,
    var duration: Duration?,
    var end_location: Location?,
    var html_instructions: String,
    var polyline: PolyLine?,
    var start_location: Location?,
    var travel_mode: String
)