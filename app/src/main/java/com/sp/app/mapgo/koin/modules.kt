package com.sp.app.mapgo.koin

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import com.sp.app.mapgo.ui.viewmodel.MainViewModel
import com.sp.app.mapgo.ui.viewmodel.MapCtlViewModel
import com.sp.app.mapgo.ui.viewmodel.GameViewModel
import com.sp.app.maplib.repository.MapRepository


val factoryModule = module {
//    factory {
//
//    }
}

val apiModule = module {

}

val repositoryModule = module {
    single {
        MapRepository(this.androidContext())
    }
}

val viewModelModule = module {
    viewModel {
        MainViewModel(get())
        MapCtlViewModel(get())
        GameViewModel(this.androidContext(), get())
    }
}