package com.sp.app.mapgo.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.sp.app.maplib.repository.MapRepository


class MainViewModel(
    val mapRepository: MapRepository,
) : ViewModel() {

    companion object {
        const val TAG = "MainViewModel"
    }

}