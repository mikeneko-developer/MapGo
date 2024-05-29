package com.sp.app.mapgo.koin

import com.sp.app.mapgo.ui.viewmodel.GameViewModel
import com.sp.app.mapgo.ui.viewmodel.MainViewModel
import com.sp.app.maplib.repository.MapRepository
import com.sp.app.maplib.ui.map.MapViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module


val factoryModule = module {
//    factory {
//
//    }
}

val apiModule = module {

}

val repositoryModule = module {
    single{
        MapRepository(this.androidContext(), 1)
    }
}

val viewModelModule = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::GameViewModel)
    viewModelOf(::MapViewModel)
}