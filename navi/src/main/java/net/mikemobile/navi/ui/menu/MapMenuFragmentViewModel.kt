package net.mikemobile.navi.ui.menu

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import net.mikemobile.navi.repository.MapRepository
import net.mikemobile.navi.system.BaseViewModel


class MapMenuFragmentViewModel(val mapRepository: MapRepository) : BaseViewModel() {

    var navigator: MapMenuFragmentNavigator? = null
    val handler = Handler()

    val pInp: LiveData<Boolean> = mapRepository.pictureInPicture



    fun initialize() {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun buttonClickMenu() {
        android.util.Log.i("!!!!!!!!!!","buttonClickMenu")
        navigator?.onClickMenu()
    }

    fun buttonClickFavorite() {
        android.util.Log.i("!!!!!!!!!!","buttonClickFavorite")
        navigator?.onClickFavorite()
    }

    fun buttonClickHome() {
        android.util.Log.i("!!!!!!!!!!","buttonClickHome")
        navigator?.onClickHome()
    }

    fun buttonClickPinP() {
        android.util.Log.i("!!!!!!!!!!","buttonClickPinP")
        mapRepository.pictureInPicture.postValue(true)
        navigator?.onClickPictureInPicture()
    }
}