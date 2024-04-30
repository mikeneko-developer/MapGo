package net.mikemobile.navi.ui.navi

import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import net.mikemobile.databindinglib.base.BaseFragment
import net.mikemobile.databindinglib.base.observeOnChanged
import net.mikemobile.navi.data.map.MapLocation
import net.mikemobile.navi.data.map.MapRoute
import net.mikemobile.navi.repository.DataRepository
import net.mikemobile.navi.repository.MapRepository
import net.mikemobile.navi.repository.RouteRepository
import net.mikemobile.navi.repository.callback.MapRepositoryCallback
import net.mikemobile.navi.repository.callback.RouteRepositoryCallback
import net.mikemobile.navi.core.geocode.GoogleRouteSearch
import net.mikemobile.navi.repository.RoboConnectRepository
import net.mikemobile.navi.repository.data.TwoPoint
import net.mikemobile.navi.ui.navi.adapter.ItemTouchCallback
import net.mikemobile.navi.ui.navi.adapter.ItemTouchCallbackListener
import net.mikemobile.navi.ui.navi.adapter.MiniCheckPointRecyclerAdapter
import androidx.recyclerview.widget.DefaultItemAnimator





class MapTopViewModel(val mapRepository: MapRepository,
                      val routeRepository: RouteRepository,
                      val dataRepository: DataRepository,
                      var roboConnectRepository: RoboConnectRepository
) : ViewModel(),
    MapRepositoryCallback, RouteRepositoryCallback {

    companion object {
        const val TAG = "MapTopViewModel"
    }

    var selectPointNumber = 1
    var navigator: MapTopFragmentNavigator? = null
    val handler = Handler()
    var listEnable = MutableLiveData<Boolean>().apply{value = false}


    val adapter = MiniCheckPointRecyclerAdapter(object:
        MiniCheckPointRecyclerAdapter.OnItemClickListener {
        override fun onItemClick(position: Int, point: MapLocation) {
            routeRepository.setFocusLocation(point.point)
        }
    })
    var listView: RecyclerView? = null
    fun setRecyclerView(view: RecyclerView, context: Context?) {
        listView = view
        listView?.setLayoutManager(LinearLayoutManager(context))
        listView?.setAdapter(adapter)
    }

    val itemTouchCallbackListener = object: ItemTouchCallbackListener {
        override fun onMove(fromPos: Int, toPos: Int) {
            //adapter.notifyItemMoved(fromPos, toPos)
            adapter.moveItem(fromPos, toPos)
            mapRepository.setMovePointMarker(fromPos, toPos)
            //adapter.getItem(fromPos)?.let { mapRepository.setSelectPoint(it) }
        }

        override fun onSwiped(fromPos: Int) {
            val item = adapter.deleteItem(fromPos)
            item?.let {
                mapRepository.setDeleteSelectPointMarker(it)
            }
        }
    }

    val itemTouchCallBack = ItemTouchCallback(this.itemTouchCallbackListener)
    val mIth = ItemTouchHelper(itemTouchCallBack)


    ////////////////////////////////////////////////////////////////////////////////////////////////
    val selectPointMarkersObserver =  object : Observer<ArrayList<MapLocation>?> {
        override fun onChanged(seletPointMarkers: ArrayList<MapLocation>?) {
            if (seletPointMarkers == null || seletPointMarkers.size == 0) {
                selectPointNumber = 1
                listEnable.postValue(false)
            } else {
                listEnable.postValue(true)
            }
        }
    }
    fun initialize(fragment: BaseFragment) {
        Log.i(TAG + " ADD_MAKER","initialize")
        mapRepository.addMapCallback("MapTopViewModel", this)
        routeRepository.addMapCallback("MapTopViewModel", this)

        adapter.setList(mapRepository.getRouteList())
    }

    fun resume(fragment: BaseFragment) {

        Log.i(TAG + " ADD_MAKER","resume")

        mapRepository.selectPointMarkersLiveData.observeOnChanged(fragment,selectPointMarkersObserver)
        mapRepository.selectLongTapPoint.observeOnChanged(fragment,observer)

        //adapter.setList(mapRepository.getRouteList())

    }

    fun pause(fragment: BaseFragment) {
        Log.i(TAG + " ADD_MAKER","pause")
        mapRepository.selectLongTapPoint.removeObservers(fragment)
        mapRepository.selectPointMarkersLiveData.removeObservers(fragment)
        //routeRepository.reset()
    }

    fun destroy() {
        mapRepository.removeMapCallback("MapTopViewModel")
        routeRepository.removeMapCallback("MapTopViewModel")

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    fun buttonClickMenu() {

        dataRepository.listDialogList.clear()
        dataRepository.favoriteList.value?.let{ list->
            list.forEach {
                dataRepository.listDialogList.add(it.name)
            }
        }

        navigator?.openMenu()
    }

    fun buttonClickRouteSearch() {

        roboConnectRepository.sayText("徒歩のルートを検索するね")
        startSearch(GoogleRouteSearch.Companion.ROUTE_MODE.WORKING)
    }

    fun buttonClickCarRouteSearch() {

        roboConnectRepository.sayText("車のルートを検索するね")
        startSearch(GoogleRouteSearch.Companion.ROUTE_MODE.DRIVING)
    }

    fun buttonClickRouteClear() {
        // 選択ポイントをクリアする
        adapter.setList(ArrayList<MapLocation>())
        mapRepository.clearSelectPoint()
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////
    fun setAddFavorite(pointData: MapLocation) {
        Log.i(TAG + " ADD_MAKER","setAddFavorite")
        dataRepository.saveFavorite(pointData)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 地図の長押しによる地点選択の監視と検知時のイベント実行
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 長押し監視
    val observer = Observer<MapLocation?> { location -> longTapSelectPoint(location) }

    /**
     * 長押し検知時に呼び出されるメソッド
     * @param pointData 位置情報
     */
    private fun longTapSelectPoint(pointData: MapLocation?) {
        if (pointData == null) {
            Log.i(TAG, "longTapSelectPoint() pointData is null")
            return
        }
        // 地点データを削除
        mapRepository.selectLongTapPoint.postValue(null)

        if (pointData.name == "選択位置") {
            pointData.name = "選択位置 " + selectPointNumber
            selectPointNumber++
        }


        // 現在の選択地点のリスト取得
        val list = mapRepository.getSelectPointList()

        // ダイアログを呼び出す
        navigator?.openDialogSelectPoint(pointData, list.size > 0)

    }

    /**
     * ダイアログから目的地・経由地のいづれかが選択された
     * @param pointData 位置情報
     */
    fun selectMaker(pointData: MapLocation) {
        Log.i(TAG + " ADD_MAKER","selectMaker")

        //val name = "選択位置" + nameCount
        //pointData.name = name

        val position = mapRepository.setSelectPointMaker(pointData)
        //Toast.makeText(mapRepository.context, "" + position, Toast.LENGTH_SHORT).show()
        if (position == -1) {
            adapter.changeItem(pointData)
        } else {
            adapter.addItem(position, pointData)
            if (pointData.haveGoal) {
                roboConnectRepository.sayText("目的地に"+pointData.name+"を追加するね")
            } else {
                roboConnectRepository.sayText("経由地に"+pointData.name+"を追加するね")
            }
        }
    }

    // 選択したポイントを削除する
    fun setClearPoint(pointData: MapLocation) {
        Log.i(TAG + " ADD_MAKER","setClearPoint")
        roboConnectRepository.sayText(pointData.name + "を削除するよ")
        mapRepository.setDeleteSelectPointMarker(pointData)
        adapter.deleteItem(pointData)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private var marker: Marker? = null
    override fun onMarkerClick(type: Int, marker: Marker?) {
        this.marker = marker
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onResultRoute(data: MapRoute) {
        // ルート情報が取得できたので、画面描画用のRepositoryに渡す
        adapter.setList(ArrayList<MapLocation>())
        navigator?.onSelectRouteMap()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 選択マーカー関連

    override fun onClickSelectPoint(type: Int, markerId: String) {
        Log.i(TAG + " SELECT_POINT","onClickSelectPoint type:" + type)
        Log.i(TAG + " SELECT_POINT","onClickSelectPoint markerId:" + markerId)

        val selectPointList = mapRepository.getSelectPointList()
        val favoriteList = mapRepository.getFavoriteMarkerList()
        Log.i(TAG + " SELECT_POINT","onClickSelectPoint size:" + selectPointList.size)
        if (type == 1) {
            // 選択位置

            var item: MapLocation? = null

            for(num in 0 until selectPointList.size) {
                if (selectPointList[num].marker_id == markerId) {
                    item = selectPointList[num]
                    item.newPosition = false
                    item.favorite = false
                }
            }

            item?.let {
                // ダイアログを表示する
                navigator?.openDialogSelectPoint(it, selectPointList.size > 1)
            }
        } else if(type == 2) {
            var item: MapLocation? = null

            for (num in 0 until favoriteList.size) {
                if (favoriteList[num].marker_id == markerId) {
                    item = favoriteList[num]
                    item.newPosition = true
                    break
                }
            }

            var selectItem: MapLocation? = null
            if (item != null) {
                for (num in 0 until selectPointList.size) {
                    if (selectPointList[num].name == item!!.name
                        && selectPointList[num].point.latitude == item!!.point.latitude
                        && selectPointList[num].point.longitude == item!!.point.longitude) {
                        selectItem = selectPointList[num]
                        item = selectItem.copy()
                        item.newPosition = false
                        break
                    }
                }

                if (selectItem == null) {
                    item!!.newPosition = true
                }
            } else {

                for(num in 0 until selectPointList.size) {
                    if (selectPointList[num].marker_id == markerId) {
                        selectItem = selectPointList[num]
                    }
                }
            }

            val haveGoal = if (selectItem != null) {
                    (selectPointList.size > 1)
            } else {
                    (selectPointList.size > 0)
            }

            // ダイアログを表示する

            item?.let {
                // ダイアログを表示する
                navigator?.openDialogSelectPoint(it, haveGoal)
            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////
    fun startSearch(mode: GoogleRouteSearch.Companion.ROUTE_MODE){
        // 現在位置の取得
        val routeList = mapRepository.getRouteData()

        if (routeList == null) {
            // ルート情報がない
            return
        }

        var ne_lat = routeList[0].point.latitude
        var ws_lat = routeList[1].point.latitude
        var ne_lon = routeList[0].point.longitude
        var ws_lon = routeList[1].point.longitude
        for(point in routeList) {
            if (ne_lat > point.point.latitude) {
                ne_lat = point.point.latitude
            } else if (ws_lat < point.point.latitude) {
                ws_lat = point.point.latitude
            }

            if (ne_lon > point.point.longitude) {
                ne_lon = point.point.longitude
            } else if(ws_lon < point.point.longitude) {
                ws_lon = point.point.longitude
            }
        }

        val ne = LatLng(ne_lat, ne_lon)
        val ws = LatLng(ws_lat, ws_lon)

        mapRepository.twoPointLiveData.postValue(TwoPoint(ne, ws))

        // 現在位置とマーカーの位置が取れたのでルート計算処理を投げる
        routeRepository.searchRoute(mode, routeList)
    }
}