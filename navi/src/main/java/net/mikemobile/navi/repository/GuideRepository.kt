package net.mikemobile.navi.repository

import android.content.Context
import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import net.mikemobile.navi.core.gps.onMyLocationListener
import net.mikemobile.navi.data.map.MapCheckPoint
import net.mikemobile.navi.data.map.MapRoute
import net.mikemobile.navi.data.old.StepItem
import net.mikemobile.navi.repository.data.ThreePoint


import net.mikemobile.navi.util.distanceCalc

class GuideRepository(var context: Context): onMyLocationListener {

    // ルートデータ
    var routesData: MapRoute? = null

    ///////////////////////////////////////////////////////////////////////////



    ///////////////////////////////////////////////////////////////////////////
    var isMapLocationEnable = false

    // 次の地点までの残距離
    var nextDistance  = MutableLiveData<Int>().apply{value = -1}
    var nextGuidance  = MutableLiveData<String>().apply{value = ""}

    var changeNextItem  = MutableLiveData<Int>().apply{value = -1}

    var guideList = ArrayList<MapCheckPoint>()
    var nextPosition = 0
    var nextPositionGuidance = -1


    ///////////////////////////////////////////////////////////////////////////
    override fun onGpsLocation(context: Context?, location: Location?) {
        location?.let {
            //checkNextDistance(LatLng(location.latitude, location.longitude))
        }
    }
    override fun onNetworkLocation(context: Context?, location: Location?) {
        location?.let {
            //checkNextDistance(LatLng(location.latitude, location.longitude))
        }
    }
    override fun onNetworkLogLocation(context: Context?, location: Location?) {}
    override fun onGpsLogLocation(context: Context?, location: Location?) {}

    ///////////////////////////////////////////////////////////////////////////

    override fun onCurrentLocation(context: Context?, location: Location) {
        //checkNextDistance(LatLng(location.latitude, location.longitude))
    }

    var currentPosition = LatLng(0.0,0.0)
    fun setLocationCurrent(current: LatLng) {
        currentPosition = current
        checkNextDistance(current)
    }

    ///////////////////////////////////////////////////////////////////////////
    // 現在地点
    var guideMode  = MutableLiveData<String?>().apply{value = null}
    fun guideStart() {
        guideMode.postValue("guide")
    }
    fun guideStop() {
        guideMode.postValue(null)
    }

    var nextPointDistance = -1
    var nextNextPointDistance = -1

    fun calcNextPoint(nextPoint: LatLng): Int {

        val distance = distanceCalc(
            currentPosition.latitude, currentPosition.longitude,
            nextPoint.latitude, nextPoint.longitude
        )

        return distance
    }
    fun checkNextDistance(current: LatLng){

        if(guideList.size == 0){
            //android.util.Log.i("GuideRepository", "ガイドリストの数が0")
            return
        }

        if(nextPosition == -1 ){
            //android.util.Log.i("GuideRepository", "nextPosition == -1")
            return
        }

        if(guideList.size <= nextPosition){
            // 最終地点終了
            android.util.Log.i("GuideRepository", "最終地点終了")
            return
        }

        if (routesData?.startPoint == null) {
            android.util.Log.i("GuideRepository", "案内開始地点が無い")
            return
        }

        val nextCheckPoint = LatLng(
                    guideList[nextPosition].end_location!!.lat!!,
                    guideList[nextPosition].end_location!!.lng!!
        )

        val threePoint = ThreePoint(nextCheckPoint, null, null)

        // 次の目的地までの距離が10mを切った場合
        if (threePoint.approachingPoint(current)) {
            android.util.Log.i("GuideRepository2", "次のチェックポイントとの距離が10m以内")
            nextPointDistance = -1
            nextNextPointDistance = -1
            // 次の中継地点に切り替え
            nextPosition++
            changeNextItem.postValue(nextPosition)
            return
        }

        if (nextPosition + 1 < guideList.size - 1) {
            val nextCheckPoint2 = LatLng(
                guideList[nextPosition + 1].end_location!!.lat!!,
                guideList[nextPosition + 1].end_location!!.lng!!
            )

            threePoint.point2 = nextCheckPoint2


            if (nextPointDistance == -1) {
                nextPointDistance = threePoint.getDistance(current)
            }

            if (nextNextPointDistance == -1) {
                // 最初の判定用のためデータを追加する
                nextNextPointDistance = threePoint.getNextNextDistance(current)
            }

            val nextDistance = threePoint.getDistance(current)
            val nextNDistanceMove = nextNextPointDistance - nextDistance

            val nextNextDistance = threePoint.getNextNextDistance(current)
            val nextNextDistanceMove = nextNextPointDistance - nextNextDistance


            if (nextNDistanceMove > 0 && nextNextDistanceMove > 0) {
                android.util.Log.i("GuideRepository2", "どちらにも近づいている")
                android.util.Log.i("GuideRepository2", "nextNDistanceMove:" + nextNDistanceMove)
                android.util.Log.i("GuideRepository2", "nextNextDistanceMove:" + nextNextDistanceMove)
                nextPointDistance = -1
                nextNextPointDistance = -1
            } else if (nextNextDistanceMove >= 30) {
                android.util.Log.i("GuideRepository2", "次の次のチェックポイントに10m以上近づいた")
                nextPointDistance = -1
                nextNextPointDistance = -1
            }

            // 次の次の目的地までの距離が10mを切った場合
            if (threePoint.checkNextNextPoint(current)) {
                android.util.Log.i("GuideRepository2", "次の次のチェックポイントとの方に近づいている")
                nextPointDistance = -1
                nextNextPointDistance = -1
                // 次の中継地点に切り替え
                nextPosition++
                changeNextItem.postValue(nextPosition)
                return
            }
        }



        android.util.Log.i("GuideRepository2", "まだチェックポイントに近づいていません")
        if (true) {
            return
        }


        // 次のチェックポイント
        var startPoint = routesData?.startPoint!!
        if (nextPosition > 0) {
            startPoint = guideList[nextPosition - 1].end_location!!
        }


        val stepItem = guideList[nextPosition]
        val nextPoint = stepItem.end_location ?: return

        threePoint.point2 = LatLng(nextPoint.lat!!, nextPoint.lng!!)

        if (guideList.size - 1 > nextPosition) {
            // 次の次のチェックポイントがある

            // 次の次のチェックポイント
            val next2Point = guideList[nextPosition+1] ?: return

            threePoint.point3 = LatLng(next2Point.end_location!!.lat!!, next2Point.end_location!!.lng!!)


            // 現在地から次のチェックポイントまでの距離
            nextDistance.postValue(threePoint.getDistance(current))

            // 次の目的地までの距離が10mを切った場合
            if (threePoint.approachingPoint(current)) {
                android.util.Log.i("GuideRepository2", "チェックポイントとの距離が5m以内")
                // 次の中継地点に切り替え
                nextPosition++
                changeNextItem.postValue(nextPosition)
                return
            }

            // 距離が次の次のチェックポイントの方が近ければ地点切り替えをする
            if (threePoint.checkNextNextPoint(current)) {
                android.util.Log.i("GuideRepository2", "チェックポイントとの距離の近さ")
                // 次の中継地点に切り替え
                nextPosition++
                changeNextItem.postValue(nextPosition)
                return
            }

            if (threePoint.checkNextNextPoint(current)) {


                return
            }

        } else {
            // 最後のチェックポイント
            nextDistance.postValue(threePoint.getDistance(current))


            if(threePoint.approachingPoint(current) && nextPosition != nextPositionGuidance){
                nextPositionGuidance = nextPosition
                nextGuidance.postValue(stepItem.html_instructions)
            }

            if(threePoint.approachingPoint(current)){
                // 次の中継地点に切り替え
                nextPosition++
                changeNextItem.postValue(nextPosition)
            }
        }
    }

    private fun getArea(ax: Double, ay: Double, bx: Double, by: Double,  cx: Double, cy: Double): Double {
        return Math.abs((cx-bx)*(ay-by)-(ax-bx)*(cy-by))/2
    }

    private fun getKeta(num: Double): Double {
        val count = 10000
        val keta = (num * count).toInt()
        return keta.toDouble()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // 距離判定と次のチェックポイントの切り替え

    fun calcChecKPointDistance(current: LatLng) {
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    fun reset(routeData: MapRoute) {
        this.routesData = routeData

        nextPosition = 0
        nextPositionGuidance = -1
        changeNextItem.postValue(0)
    }

    fun reset(list: ArrayList<MapCheckPoint>){
        guideList = list.clone() as ArrayList<MapCheckPoint>

        nextPosition = 0
        nextPositionGuidance = -1
        changeNextItem.postValue(0)
    }

}