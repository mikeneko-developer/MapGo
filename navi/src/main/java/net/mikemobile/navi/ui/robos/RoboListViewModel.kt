package net.mikemobile.navi.ui.robos

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.mikemobile.databindinglib.base.BaseFragment
import net.mikemobile.databindinglib.base.observeOnChanged
import net.mikemobile.navi.data.robo.RoboData
import net.mikemobile.navi.repository.DataRepository
import net.mikemobile.navi.repository.RoboConnectRepository


class RoboListViewModel(var roboConnectRepository: RoboConnectRepository, var dataRepository: DataRepository) : ViewModel(),
    RoboListItemClickListener {

    var navigator: RoboListFragmentNavigator? = null
    val handler = Handler()

    lateinit var recyclerListView: RecyclerView
    var adapter: RobohonListAdapter? = null


    fun initialize() {
        android.util.Log.i("MapRouteViewModel","initialize")
    }

    fun setRecyclerView(context: Context, recyclerView: RecyclerView){
        recyclerListView = recyclerView

        recyclerListView.setHasFixedSize(true)

        var manager = LinearLayoutManager(context)
        manager.setOrientation(LinearLayoutManager.VERTICAL) // ここで横方向に設定
        // use a linear layout manager
        recyclerListView.layoutManager = manager

        // specify an viewAdapter (see also next example)
        adapter = RobohonListAdapter(this)
        recyclerListView.adapter = adapter

    }

    fun initializeList(){
        roboConnectRepository.readRoboList()
    }

    fun resume(fragment: BaseFragment) {
        roboConnectRepository.roboList.observeOnChanged(fragment, observerList)
        roboConnectRepository.connectDevice.observe(fragment,observerDevice)
    }

    fun pause(fragment: BaseFragment) {
        roboConnectRepository.roboList.removeObservers(fragment)
        roboConnectRepository.connectDevice.removeObservers(fragment)

    }

    fun destroy() {
    }

    val observerList = object: Observer<MutableList<RoboData>> {
        override fun onChanged(list: MutableList<RoboData>) {
            adapter?.list = list
            adapter?.notifyDataSetChanged()
            recyclerListView?.invalidate()
        }
    }

    val observerDevice = object: Observer<BluetoothDevice> {
        override fun onChanged(item: BluetoothDevice?) {

            adapter?.selectData = item
            adapter?.notifyDataSetChanged()
            recyclerListView?.invalidate()
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    var list: MutableList<BluetoothDevice>? = null
    fun buttonClickAdd(){
        list = roboConnectRepository.getBluetoothDevice()

        var nameList = mutableListOf<String>()

        list?.forEach{
            nameList.add(it.address + "\n" + it.name)
        }

        dataRepository.listDialogList = nameList
        navigator?.onClickOpenDialog()

    }

    fun buttonClickConnect() {
        if(roboConnectRepository.deviceAddress == null){
            return
        }

        if(roboConnectRepository.bluetoothStatus){
            roboConnectRepository.disconnect()
        }else {
            roboConnectRepository.connect(roboConnectRepository.deviceAddress!!)
        }
    }

    fun buttonClickTalk() {
        roboConnectRepository.sayText("喋るテストだよ")
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Bluetoothリストからアイテムを選択された
    fun selectItemPosition(position: Int){
        list?.let{
            var item = it[position]
            roboConnectRepository.deviceAddress = item.address

            var roboData = RoboData(0,"ロボホン", item.address)
            roboConnectRepository.saveRoboData(roboData)

            roboConnectRepository.connect(item.address)
        }
    }

    override fun clickItem(position: Int, robo: RoboData) {
        //roboConnectRepository.deviceAddress = robo.address

    }

    override fun clickConnect(position: Int, robo: RoboData) {
        roboConnectRepository.connect(robo.address)
    }

}