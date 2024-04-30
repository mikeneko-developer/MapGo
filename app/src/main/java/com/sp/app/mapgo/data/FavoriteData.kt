package com.sp.app.mapgo.data

data class FavoriteData(var id: Int,
                        var name: String,
                        var address: String,
                        var lat: Double,
                        var lon: Double){
    constructor():this(0,"","",0.0,0.0)
}