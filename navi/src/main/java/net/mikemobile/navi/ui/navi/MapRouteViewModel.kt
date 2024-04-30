package net.mikemobile.navi.ui.navi

import android.location.Location
import android.os.Handler
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import net.mikemobile.databindinglib.base.BaseFragment
import net.mikemobile.navi.R
import net.mikemobile.navi.repository.GuideRepository
import net.mikemobile.navi.repository.MapRepository
import net.mikemobile.navi.repository.RouteRepository
import net.mikemobile.navi.ui.util.custom_holizontal_view.CustomRecyclerView
import net.mikemobile.navi.ui.util.custom_holizontal_view.CustomRecyclerViewListener
import net.mikemobile.navi.ui.util.custom_holizontal_view.HorizontalListAdapter
import net.mikemobile.navi.util.Constant.Companion.ZOOM_ONE_SIZE
import net.mikemobile.navi.util.MyDate
import net.mikemobile.navi.util.distanceCalc


class MapRouteViewModel(val mapRepository: MapRepository,
                        val routeRepository: RouteRepository,
                        val guideRepository: GuideRepository
) : ViewModel() {

    var navigator: MapRouteFragmentNavigator? = null
    val handler = Handler()

    lateinit var holizontalListView:CustomRecyclerView
    var holizontalListAdapter: HorizontalListAdapter? = null

    val NUMBER_OF_SEARCH_ADDRESSES = 6

    var listPosition = 0
    var nextPoint:LatLng? = null

    var view_text = MutableLiveData<String>().apply {
        value = ""
    }

    fun setRecyclerView(recyclerView: CustomRecyclerView){
        holizontalListView = recyclerView

        holizontalListView.setOnViewListener(object: CustomRecyclerViewListener {
            override fun onRendering(width: Int, height: Int) {
                android.util.Log.i("MapRouteViewModel","holizontalList onRendering()")
                initializeList()
            }

            override fun onSelectPosition(position: Int) {
                android.util.Log.i("MapRouteViewModel","onSelectPosition("+position+")")
                setSelectPosition(position)
            }
        })

    }

    fun initialize() {
        android.util.Log.i("MapRouteViewModel","initialize")

    }

    fun initializeList(){
        holizontalListView.orientationType = LinearLayoutManager.HORIZONTAL
        holizontalListView.setHasFixedSize(true)

        var manager = LinearLayoutManager(mapRepository.context)
        manager.setOrientation(LinearLayoutManager.HORIZONTAL) // ここで横方向に設定
        // use a linear layout manager
        holizontalListView.layoutManager = manager


        val addressListHeight = holizontalListView.height
        val paddingVertical = addressListHeight / NUMBER_OF_SEARCH_ADDRESSES

        //recyclerView.setItemCount(ListItemEnableCount)
        holizontalListView.setItemHeight(paddingVertical)

        // specify an viewAdapter (see also next example)
        holizontalListAdapter = HorizontalListAdapter()
        holizontalListAdapter?.orientation = LinearLayoutManager.HORIZONTAL
        holizontalListView.adapter = holizontalListAdapter


        for(item in routeRepository.getRoute()) {
            //Log.i("TESTTESTTESTTEST","" + item.html_instructions)
        }

        holizontalListAdapter?.list = routeRepository.getRoute()
        updateList()

        //setSelectPosition(0)

    }

    fun resume(fragment: BaseFragment) {
        // 現在位置が更新されたら取得する
        routeRepository.currentPositionLiveData.observe(fragment, object: Observer<LatLng?> {
            override fun onChanged(latLng: LatLng?) {
                if(latLng != null){
                    //Log.v("!!!!!!!!!!","位置情報がが更新されました")
                    checkDistance(latLng)
                }
            }
        })

    }

    fun pause(fragment: BaseFragment) {
        routeRepository.currentPositionLiveData.removeObservers(fragment)

    }

    fun destroy() {
    }

    fun onBack() {
        routeRepository.clearNextPoint()
        routeRepository.routeClear()
        mapRepository.clearStartPoint()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun buttonClickGuideStart() {
        guideRepository.reset(routeRepository.getRoute())
        guideRepository.reset(routeRepository.getRoutes())
        guideRepository.guideStart()
        navigator?.onClickGuideStart()
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    fun updateList(){
        android.util.Log.i("MapRouteViewModel","updateList")

        holizontalListAdapter?.notifyDataSetChanged()
        holizontalListView.invalidate()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * リストの選択位置時のデータ更新
     */
    fun setSelectPosition(position: Int){
        listPosition = position

        val list = routeRepository.getRoute()
        if (list.size == 0) {
            return
        } else {
            val latLng = list[position].start_location

            var nextPoint: LatLng? = null
            list[position]?.end_location?.let { end_location ->
                nextPoint = LatLng(end_location.lat!!, end_location.lng!!)
            }


            latLng?.let {
                routeRepository.setNextPoint(LatLng(it.lat!!, it.lng!!))
            }

            routeRepository.setFocusLocation(nextPoint!!)

            this.nextPoint = nextPoint
        }

        nextPoint?.let {
            checkDistance(it)
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 距離の計算
     */
    fun checkDistance(nextPoint: LatLng) {
        val nextPointtoDistance = routeRepository.calcNextPoint(nextPoint)
        view_text.postValue("距離:" + nextPointtoDistance)
    }

}