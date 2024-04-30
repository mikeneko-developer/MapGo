package net.mikemobile.navi.ui.robos

import android.os.Handler
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.mikemobile.databindinglib.base.BaseFragment
import net.mikemobile.navi.repository.RoboConnectRepository


class TalkViewModel(var roboRepository: RoboConnectRepository) : ViewModel() {

    var navigator: TalkFragmentNavigator? = null
    val handler = Handler()

    var inputText = MutableLiveData<String>().apply{value = ""}

    fun initialize() {
    }

    fun resume(fragment: BaseFragment) {

    }

    fun pause() {

    }

    fun destroy() {

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun buttonClickSend() {
        var talk = inputText.value!!
        roboRepository.sayText(talk)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////


}