package net.mikemobile.navi.ui.navi.favorite

import android.content.Context
import android.os.Handler
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.mikemobile.databindinglib.base.BaseFragment
import net.mikemobile.navi.data.old.FavoriteData
import net.mikemobile.navi.repository.DataRepository
import net.mikemobile.navi.repository.DataRepositoryListener
import net.mikemobile.navi.system.BaseViewModel


class FavoriteEditViewModel(var dataRepository: DataRepository) : BaseViewModel(),
    DataRepositoryListener {

    var navigator: FavoriteEditFragmentNavigator? = null
    val handler = Handler()

    val favoriteData = MutableLiveData<FavoriteData>().apply{
        if(dataRepository.favoriteEdit == null){
            value = FavoriteData()
        }else {
            value = dataRepository.favoriteEdit
        }
    }


    fun initialize() {
        android.util.Log.i("MapRouteViewModel","initialize")


    }

    fun setRecyclerView(context: Context, recyclerView: RecyclerView){
        recyclerView.setHasFixedSize(true)

        var manager = LinearLayoutManager(context)
        manager.setOrientation(LinearLayoutManager.VERTICAL) // ここで横方向に設定
        // use a linear layout manager
        recyclerView.layoutManager = manager

    }

    fun resume(fragment: BaseFragment) {
        /**
        dataRepository.favoriteList.observe(fragment, object: Observer<MutableList<FavoriteData>> {
            override fun onChanged(list: MutableList<FavoriteData>) {
                navigator?.onCloseFragment()
            }
        })
        */
    }

    fun pause() {

    }

    fun destroy() {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun buttonClickBack() {
        navigator?.onCloseFragment()
    }

    fun buttonClickDelete() {

        dataRepository.setOnDataRepositoryListener(this)

        favoriteData.value?.let{data ->
            //android.util.Log.i("TEST_LOG11111","title:" + data.name)
            dataRepository.deleteFavorite(data)
        }

    }

    fun buttonClickSave() {

        dataRepository.setOnDataRepositoryListener(this)

        favoriteData.value?.let{data ->
            android.util.Log.i("TEST_LOG11111","title:" + data.name)
            dataRepository.saveFavorite(data)
        }


    }

    override fun favoriteUpdate() {
        dataRepository.setOnDataRepositoryListener(null)
        navigator?.onCloseFragment()
    }

    override fun favoriteDelete() {
        navigator?.onCloseFragment()
    }


}