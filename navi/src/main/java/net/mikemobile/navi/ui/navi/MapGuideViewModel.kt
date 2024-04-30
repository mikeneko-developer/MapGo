package net.mikemobile.navi.ui.navi

import android.os.Handler
import android.view.MotionEvent
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import com.google.android.gms.maps.model.LatLng
import net.mikemobile.databindinglib.base.BaseFragment
import net.mikemobile.databindinglib.base.observeOnChanged
import net.mikemobile.navi.repository.GuideRepository
import net.mikemobile.navi.repository.MapRepository
import net.mikemobile.navi.repository.RoboConnectRepository
import net.mikemobile.navi.repository.RouteRepository
import net.mikemobile.navi.ui.map.MapViewModel
import net.mikemobile.navi.ui.util.custom_holizontal_view.CustomRecyclerView
import net.mikemobile.navi.ui.util.custom_holizontal_view.CustomRecyclerViewListener
import net.mikemobile.navi.ui.util.custom_holizontal_view.HorizontalListAdapter
import net.mikemobile.navi.util.RobohonText
import net.mikemobile.navi.util.distanceCalc
import net.mikemobile.navi.util.parseRouteNavigationText


class MapGuideViewModel(val mapRepository: MapRepository,
                        val routeRepository: RouteRepository,
                        val guideRepository: GuideRepository,
                        var roboConnectRepository: RoboConnectRepository) : ViewModel() {

    var navigator: MapGuideFragmentNavigator? = null
    val handler = Handler()

    var robohonText = RobohonText()
    val TAG = "MapGuideViewModel"

    lateinit var holizontalListView:CustomRecyclerView
    var holizontalListAdapter: HorizontalListAdapter? = null

    val NUMBER_OF_SEARCH_ADDRESSES = 6


    var view_text = MutableLiveData<String>().apply {
        value = ""
    }
    var say_text = MutableLiveData<String>().apply {
        value = ""
    }

    class ScrollController : OnItemTouchListener {
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean { return true }
        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
    }

    fun setRecyclerView(recyclerView: CustomRecyclerView){
        holizontalListView = recyclerView

        holizontalListView.setOnViewListener(object: CustomRecyclerViewListener {
            override fun onRendering(width: Int, height: Int) {
                android.util.Log.i(TAG,"holizontalList onRendering()")
                initializeList()
            }

            override fun onSelectPosition(position: Int) {
                setSelectPosition(position)
            }
        })

        var controller = ScrollController()
        //holizontalListView!!.addOnItemTouchListener(controller);

    }

    var first = false
    fun initialize() {
        android.util.Log.i(TAG,"initialize")

        //mapRepository.mapStatus.postValue(MapViewModel.MAP_STATUS.GUIDE)
        first = true
        roboConnectRepository.sayText("道案内を開始するよ！")
        say_text.postValue("道案内を開始するよ！")
        var handler = Handler()
        handler.postDelayed(object:Runnable{
            override fun run() {
                startGuide()
            }
        },3000)
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

        holizontalListAdapter?.list = guideRepository.guideList
        updateList()

    }

    fun start(fragment: BaseFragment) {

        roboConnectRepository.status.observeOnChanged(fragment, object: Observer<RoboConnectRepository.STATUS> {
            override fun onChanged(status: RoboConnectRepository.STATUS) {
                if (first) {
                    if (status == RoboConnectRepository.STATUS.SAY_END) {

                        first = false
                    }
                } else {
                    if (status == RoboConnectRepository.STATUS.SAY_END) {
                        //say_text.postValue("")
                        //Toast.makeText(mapRepository.context, "onChanged", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

        guideRepository.nextGuidance.observeOnChanged(fragment, object: Observer<String> {
            override fun onChanged(guideanceText: String) {
                if(!guideanceText.isNullOrBlank()) {
                    val sayText = robohonText.changeText(parseRouteNavigationText(guideanceText))
                    roboConnectRepository.sayText(sayText)
                    say_text.postValue(sayText)
                    Toast.makeText(guideRepository.context, sayText, Toast.LENGTH_SHORT).show()
                }
            }
        })

        guideRepository.nextDistance.observeOnChanged(fragment, object: Observer<Int> {
            override fun onChanged(distance: Int) {
                if(distance == -1){
                    view_text.postValue("")
                }else {
                    view_text.postValue("" + distance + "m")
                }
            }
        })

        guideRepository.changeNextItem.observeOnChanged(fragment, object: Observer<Int> {
            override fun onChanged(nextPosition: Int) {
                if(nextPosition != -1){
                    changeNextPoint(nextPosition)
                }
            }
        })


    }

    fun end(fragment: BaseFragment) {
        guideRepository.nextGuidance.removeObservers(fragment)
        guideRepository.nextDistance.removeObservers(fragment)
        guideRepository.changeNextItem.removeObservers(fragment)

    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    fun buttonClickEnd() {
        roboConnectRepository.sayText("道案内を終了するよ！")
        stopGuide()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    fun startGuide(){
        setSelectPosition(guideRepository.nextPosition)
    }

    fun stopGuide() {

        guideRepository.guideStop()

        // ガイド情報をクリアする
        guideRepository.reset(ArrayList())

        // 選択ポイント、ルート線を削除する
        mapRepository.clearSelectPoint()

        routeRepository.clearNextPoint()
        routeRepository.routeClear()
        mapRepository.clearStartPoint()

        navigator?.onFinishGuide()
    }

    fun setSelectPosition(position: Int){
        if (guideRepository.guideList == null || guideRepository.guideList.size == 0)return;

        val stepItem = guideRepository.guideList[position]

        var text = stepItem.html_instructions
        val sayText = robohonText.changeText(parseRouteNavigationText(text))
        roboConnectRepository.sayText(sayText)
        say_text.postValue(sayText)
        //Toast.makeText(guideRepository.context, sayText, Toast.LENGTH_SHORT).show()


        var nextPoint: LatLng? = null
        stepItem.end_location?.let{
            nextPoint = LatLng(it.lat!!,it.lng!!)
            routeRepository.setNextPoint(nextPoint!!)
        }

        // 距離を表示
        nextPoint?.let {
            checkDistance(it)
        }
    }

    fun changeNextPoint(position: Int){
        if(guideRepository.guideList.size == 0 || guideRepository.guideList.size <= position){
            return
        }

        val stepItem = guideRepository.guideList[position]

        stepItem.end_location?.let{
            routeRepository.setNextPoint(LatLng(it.lat!!,it.lng!!))
        }

        //holizontalListAdapter?.selectPosition = position
        //holizontalListAdapter?.notifyDataSetChanged()

        holizontalListView.scrollToPosition(position)

    }


    fun updateList(){
        android.util.Log.i(TAG,"updateList")

        holizontalListAdapter?.notifyDataSetChanged()
        holizontalListView.invalidate()
    }


    fun reRoute(){
        var startLatLng: LatLng? = null
        var endLatLng: LatLng? = null

        /**


        if(startLatLng != null && endLatLng != null) {
            //routeRepository.searchRoute(startLatLng!!, endLatLng!!)
        }
        */
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 距離の計算
     */
    fun checkDistance(nextPoint: LatLng) {
        val nextPointtoDistance = guideRepository.calcNextPoint(nextPoint)
        view_text.postValue("距離:" + nextPointtoDistance)
    }

}