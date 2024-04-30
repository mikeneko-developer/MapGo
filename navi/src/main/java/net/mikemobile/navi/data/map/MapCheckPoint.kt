package net.mikemobile.navi.data.map

import net.mikemobile.navi.data.old.Distance
import net.mikemobile.navi.data.old.Duration
import net.mikemobile.navi.data.old.Location
import net.mikemobile.navi.data.old.PolyLine

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