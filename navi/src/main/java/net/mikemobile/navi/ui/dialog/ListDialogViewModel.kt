package net.mikemobile.navi.ui.dialog

import android.content.Context
import android.location.Location
import android.os.Handler
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import net.mikemobile.databindinglib.base.BaseFragment
import net.mikemobile.navi.repository.DataRepository
import net.mikemobile.navi.repository.MapRepository
import net.mikemobile.navi.ui.robos.RobohonListAdapter
import net.mikemobile.navi.util.Constant.Companion.ZOOM_ONE_SIZE


class ListDialogViewModel(var dataRepository: DataRepository) : ViewModel(),
    OnListDialogItemClickListener {

    var navigator: ListDialogNavigator? = null
    val handler = Handler()


    lateinit var recyclerListView: RecyclerView
    var adapter: ListDialogAdapter? = null


    fun initialize() {

    }

    fun setRecyclerView(context: Context, recyclerView: RecyclerView){

        recyclerView.setHasFixedSize(true)

        var manager = LinearLayoutManager(context)
        manager.setOrientation(LinearLayoutManager.VERTICAL) // ここで横方向に設定
        // use a linear layout manager
        recyclerView.layoutManager = manager

        // specify an viewAdapter (see also next example)
        adapter = ListDialogAdapter(this)
        adapter?.list = dataRepository.listDialogList

        recyclerView.adapter = adapter


        this.recyclerListView = recyclerView

        recyclerListView?.invalidate()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun buttonClickClose() {
        navigator?.onCancel()
        navigator?.onCloseFragment()
    }

    override fun onClickItem(position: Int) {
        navigator?.onSelect(position)
        navigator?.onCloseFragment()
    }
}