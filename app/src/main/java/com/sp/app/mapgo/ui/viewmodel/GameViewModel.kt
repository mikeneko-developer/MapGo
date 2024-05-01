package com.sp.app.mapgo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.sp.app.maplib.repository.MapRepository


class GameViewModel(
    private val context: Context,
    val mapRepository: MapRepository,
) : ViewModel() {

    companion object {
        const val TAG = "GameViewModel"
    }
    ///////////////////////////////////////////////////
    fun initialize() {}

    fun create() {}

    fun resume() {}

    fun pause() {}

    fun destroy() {}
}