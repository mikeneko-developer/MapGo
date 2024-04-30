package net.mikemobile.navi.ui.dialog

import android.os.Handler
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.mikemobile.navi.data.map.MapLocation
import net.mikemobile.navi.repository.MapRepository


class SelectButtonDialogViewModel(val mapRepository: MapRepository) : ViewModel() {

    companion object {
        const val TAG = "SelectButtonDialogViewModel"
    }

    var navigator: SelectButtonDialogNavigator? = null
    val handler = Handler()

    var haveGoal = MutableLiveData<Boolean>().apply {
        value = false
    }
    var pointData = MutableLiveData<MapLocation>().apply {
        value = null
    }
    var newPosition = MutableLiveData<Boolean>().apply {
        value = false
    }

    var goalEnabled = MutableLiveData<Boolean>().apply {
        value = false
    }
    var viaEnabled = MutableLiveData<Boolean>().apply {
        value = false
    }
    fun initialize() {

    }

    fun setPoint(pointData: MapLocation) {
        this.pointData.postValue(pointData)

        // 新規選択位置かどうかの判定
        Log.i(TAG, "setPoint favorite:" + pointData.favorite)
        Log.i(TAG, "setPoint newPosition:" + pointData.newPosition)

        newPosition.postValue(pointData.newPosition)

        // 目的地・経由地のボタン表示判定用
        if (pointData.favorite) {
            if (pointData.newPosition) {
                goalEnabled.postValue(true)
                viaEnabled.postValue(true)
            } else if (pointData.haveGoal) {
                goalEnabled.postValue(false)
                viaEnabled.postValue(true)
            } else {
                goalEnabled.postValue(true)
                viaEnabled.postValue(false)
            }
        } else if (pointData.newPosition) {
            goalEnabled.postValue(true)
            viaEnabled.postValue(true)
        } else if (pointData.haveGoal) {
            goalEnabled.postValue(false)
            viaEnabled.postValue(true)
        } else {
            goalEnabled.postValue(true)
            viaEnabled.postValue(false)
        }

    }

    fun setHaveGoal(have_goal: Boolean) {
        Log.i(TAG, "setHaveGoal have_goal:" + have_goal)
        haveGoal.postValue(have_goal)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun buttonClickGoRoute() {
        pointData.value?.let {
            if (it.favorite && it.newPosition) {

                var pointData = it.copy()
                pointData.haveGoal = true
                navigator?.onRoute(pointData)
            } else {
                it.haveGoal = true
                navigator?.onRoute(it)
            }
        }
        navigator?.onCloseFragment()
    }
    fun buttonClickGoViaRoute() {
        pointData.value?.let {
            if (it.favorite && it.newPosition) {

                var pointData = it.copy()
                pointData.haveGoal = false
                navigator?.onViaRoute(pointData)
            } else {
                it.haveGoal = false
                navigator?.onViaRoute(it)
            }
        }
        navigator?.onCloseFragment()
    }

    fun buttonClickSavePoint() {
        pointData.value?.let {
            navigator?.onSavePoint(it)
        }
        navigator?.onCloseFragment()
    }

    fun buttonClickClearPoint() {
        pointData.value?.let {
            navigator?.onClearPoint(it)
        }
        navigator?.onCloseFragment()
    }

    fun buttonClickClose() {
        navigator?.onCancel()
        navigator?.onCloseFragment()
    }
}