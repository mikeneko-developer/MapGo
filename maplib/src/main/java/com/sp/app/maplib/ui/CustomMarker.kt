package com.sp.app.maplib.ui

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.sp.app.maplib.R

fun getSelectMarkerOptions(context: Context, text: String, latLng: LatLng): MarkerOptions {
    val options = MarkerOptions()
    options.title(text)
    options.position(latLng)
    options.alpha(0.5f)

    val bmp = getImage(context, R.drawable.marker_brack)
    bmp?.let {
        val icon = BitmapDescriptorFactory.fromBitmap(bmp)
        options.icon(icon)
    }

    return options
}


private var mapIconImage = mutableMapOf<Int, Bitmap>()
fun getImage(context: Context, resource_id: Int): Bitmap? {
    if(mapIconImage.containsKey(resource_id)){
        val bitmap = mapIconImage.get(resource_id)
        if(bitmap != null) {
            return bitmap
        }
    }

    val r: Resources = context.getResources()
    var bitmap = BitmapFactory.decodeResource(r, resource_id)

    mapIconImage.set(resource_id, bitmap)

    return bitmap
}