package net.mikemobile.navi.ui.menu

import android.content.Context
import android.os.Handler
import androidx.lifecycle.ViewModel
import net.mikemobile.navi.system.BaseViewModel
import net.mikemobile.navi.ui.map.MapFragment
import net.mikemobile.navi.ui.navi.MapTopFragment
import net.mikemobile.navi.ui.robos.RoboListFragment


class RoboMenuFragmentViewModel() : BaseViewModel() {

    var navigator: RoboMenuFragmentNavigator? = null
    val handler = Handler()

    fun initialize() {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun buttonClickMenu() {
        android.util.Log.i("!!!!!!!!!!","buttonClickMenu")
        navigator?.onClickMenu()
    }

    fun buttonClickRobo() {
        android.util.Log.i("!!!!!!!!!!","buttonClickRobo")
        navigator?.onClickRobo()
    }

    fun buttonClickTalk() {
        android.util.Log.i("!!!!!!!!!!","buttonClickTalk")
        navigator?.onClickTalk()
    }

    fun buttonClickImage() {
        android.util.Log.i("!!!!!!!!!!","buttonClickImage")
        navigator?.onClickImage()
    }
}