package net.mikemobile.navi.ui.navi.favorite

import android.content.Context
import android.os.Handler
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.mikemobile.databindinglib.base.BaseFragment
import net.mikemobile.databindinglib.base.observeOnChanged
import net.mikemobile.navi.data.old.FavoriteData
import net.mikemobile.navi.repository.DataRepository
import net.mikemobile.navi.repository.MapRepository
import net.mikemobile.navi.system.BaseViewModel


class FavoriteListViewModel(
    var dataRepository: DataRepository,
    val mapRepository: MapRepository
    ) : BaseViewModel(), FavoriteListItemClickListener {

    var navigator: FavoriteListFragmentNavigator? = null
    val handler = Handler()

    lateinit var recyclerListView: RecyclerView
    var adapter: FavoriteListAdapter? = null


    fun initialize() {
        android.util.Log.i("MapRouteViewModel","initialize")
    }

    fun setRecyclerView(context: Context, recyclerView: RecyclerView){
        recyclerView.setHasFixedSize(true)

        var manager = LinearLayoutManager(context)
        manager.setOrientation(LinearLayoutManager.VERTICAL) // ここで横方向に設定
        // use a linear layout manager
        recyclerView.layoutManager = manager

        // specify an viewAdapter (see also next example)
        adapter =
            FavoriteListAdapter(this)
        recyclerView.adapter = adapter

        this.recyclerListView = recyclerView
    }

    fun initializeList(){
        var list = dataRepository.favoriteList.value

        list?.let{
            adapter?.list = it
        }

        recyclerListView?.invalidate()
    }

    fun resume(fragment: BaseFragment) {
        dataRepository.favoriteList.observeOnChanged(fragment, object: Observer<MutableList<FavoriteData>> {
            override fun onChanged(list: MutableList<FavoriteData>) {
                adapter?.list = list
                recyclerListView?.invalidate()
            }
        })
    }

    fun pause() {

    }

    fun destroy() {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun buttonClickBack() {
        navigator?.onCloseFragment()
    }


    override fun clickItem(position: Int, favoriteData: FavoriteData) {
        dataRepository.favoriteEdit = favoriteData
        navigator?.onClickEdit(position)
    }

    override fun clickSelect(position: Int, favoriteData: FavoriteData) {
        // 位置の移動
        mapRepository.selectFavorite(favoriteData)
        navigator?.onCloseFragment()
    }

}