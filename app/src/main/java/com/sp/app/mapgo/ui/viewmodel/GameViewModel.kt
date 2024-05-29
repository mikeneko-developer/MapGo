package com.sp.app.mapgo.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel


class GameViewModel(
    private val context: Context,
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