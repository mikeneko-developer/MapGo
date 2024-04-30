package net.mikemobile.navi.system

import android.app.Activity
import android.os.Bundle
import net.mikemobile.databindinglib.BaseActivityApplication
import net.mikemobile.databindinglib.base.ActivityNavigator
import net.mikemobile.databindinglib.base.BaseFragmentFactory
import net.mikemobile.navi.repository.*
import net.mikemobile.navi.ui.dialog.ListDialogViewModel
import net.mikemobile.navi.ui.menu.MenuFragmentViewModel
import net.mikemobile.navi.ui.dialog.SelectButtonDialogViewModel
import net.mikemobile.navi.ui.navi.MapGuideViewModel
import net.mikemobile.navi.ui.navi.MapRouteViewModel
import net.mikemobile.navi.ui.navi.MapTopViewModel
import net.mikemobile.navi.ui.map.MapViewModel
import net.mikemobile.navi.ui.menu.MapMenuFragmentViewModel
import net.mikemobile.navi.ui.menu.RoboMenuFragmentViewModel
import net.mikemobile.navi.ui.navi.favorite.FavoriteEditViewModel
import net.mikemobile.navi.ui.navi.favorite.FavoriteListViewModel
import net.mikemobile.navi.ui.robos.RoboListViewModel
import net.mikemobile.navi.ui.robos.TalkViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module


class DataBindingApplication : BaseActivityApplication() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {

    }

    override fun onFragmentFractory(): BaseFragmentFactory {
        return FragmentFactory()
    }

    override fun onViewModule(): Module {
        return module {
            viewModel { MapViewModel(get(), get(), get(), get(), get()) }
            viewModel { MapTopViewModel(get(), get(), get(), get()) }
            viewModel { MapRouteViewModel(get(), get(), get()) }
            viewModel { MapGuideViewModel(get(), get(), get(), get()) }
            viewModel { FavoriteListViewModel(get(), get()) }
            viewModel { FavoriteEditViewModel(get()) }

            viewModel { RoboListViewModel(get(),get()) }
            viewModel { TalkViewModel(get()) }


            viewModel { MenuFragmentViewModel() }

            viewModel { MapMenuFragmentViewModel(get()) }
            viewModel { RoboMenuFragmentViewModel() }


            viewModel { SelectButtonDialogViewModel(get()) }
            viewModel { ListDialogViewModel(get()) }

        }
    }

    override fun onModelModule(): Module {
        return module {
            single { MapRepository(this.androidContext()) }
            single { RouteRepository(this.androidContext()) }
            single { GuideRepository(this.androidContext()) }
            single { DataRepository(this.androidContext()) }
            single { RoboConnectRepository(this.androidContext()) }
        }
    }

    override fun onNavigatorModule(): Module {
        return module {
            single { ActivityNavigator() }
        }
    }

    override fun onOtherModule(): Module {
        return module {
            //single { NetworkController() as INetworkController }
        }
    }

}