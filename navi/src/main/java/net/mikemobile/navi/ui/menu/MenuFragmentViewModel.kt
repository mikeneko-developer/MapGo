package net.mikemobile.navi.ui.menu

import android.content.Context
import android.os.Handler
import androidx.lifecycle.ViewModel
import net.mikemobile.navi.system.BaseViewModel
import net.mikemobile.navi.ui.map.MapFragment
import net.mikemobile.navi.ui.navi.MapTopFragment
import net.mikemobile.navi.ui.robos.RoboListFragment


class MenuFragmentViewModel() : BaseViewModel() {

    var navigator: MenuFragmentNavigator? = null
    val handler = Handler()

    fun initialize() {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun buttonClickMenu() {
        android.util.Log.i("!!!!!!!!!!","buttonClickMenu")
        navigator?.onCloseFragment()
    }

    fun buttonClickMap() {
        android.util.Log.i("!!!!!!!!!!","buttonClickMap")
        navigator?.onMenuSelect(MapTopFragment.TAG)
    }

    fun buttonClickRobo() {
        android.util.Log.i("!!!!!!!!!!","buttonClickRobo")
        navigator?.onMenuSelect(RoboListFragment.TAG)
    }

    fun buttonClickClearPoint() {
        navigator?.onCloseFragment()
    }
}