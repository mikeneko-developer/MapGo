package net.mikemobile.navi.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import net.mikemobile.navi.core.gps.LocateAccesser
import net.mikemobile.navi.core.gps.MagneticSensor
import net.mikemobile.navi.core.gps.MagneticSensorListener
import net.mikemobile.navi.core.gps.onMyLocationListener
import net.mikemobile.navi.repository.GuideRepository
import net.mikemobile.navi.repository.MapRepository
import net.mikemobile.navi.ui.map.MapController
import net.mikemobile.navi.util.MyNotification
import net.mikemobile.navi.util.distanceCalc
import org.koin.android.ext.android.inject


interface OnServiceListener{
    fun onUndindService()
}

class RoboAppService : Service() {

    companion object {
        const val TAG = "AlarmManagerService"
        enum class LocationType {
            GPS,           // マップ情報の読み込み中
            NETWORK,                // マップ表示状態
            LOG,    // 位置情報を直接指定して移動
        }
    }
    private val notification = MyNotification()

    private val mapRepository: MapRepository by inject()
    private val routeRepository: MapRepository by inject()
    private val guideRepository: GuideRepository by inject()


    private val notificationChannelID = "navi_notification_channel"
    private val notificationChannelNAME = "navi_channel"

    private val binder = LocalBinder()


    private var listener:OnServiceListener? = null

    inner class LocalBinder : Binder() {

        fun setListener(l:OnServiceListener?){
            listener = l
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        listener = null
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()

        /////////////////////////////////////////////////////////////////////
        // データ保存クラスの初期化
        Log.i(TAG, "onCreate() >> ")

        showNotification()

        startLocation(this)
        startSensor(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        /**
        var event = if(intent?.getStringExtra("event") == null){"none"}else{intent?.getStringExtra("event")}

        Log.i(TAG, "onStartCommand() >> event:" + event)
        if (event != null) {
            event?.let {

            }
        } else {
            // イベントが無いので終了させる
            destoryService()
        }

        */

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        stopLocation()
        stopSensor()
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // サービスに紐づいた通知を表示

    var SERVICE_NOTIFICATION_ID = 1
    private fun showNotification(){
        Log.i(TAG, "showNotification() >> ")

        startForeground(SERVICE_NOTIFICATION_ID, notification.createServiceNotification(this))
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    private fun destoryService(){
        Log.i(TAG,"destoryService()")
        listener?.onUndindService()

        val serviceIntent = Intent(this, this::class.java)
        this.applicationContext.stopService(serviceIntent)
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //var location = MyLocation(context).setGPSSetup(Criteria.POWER_MEDIUM)
    lateinit var location: LocateAccesser
    var prevCurrentLocation: Location? = null

    var firstLoadLocation = false
    fun startLocation(context: Context){
        firstLoadLocation = true
        location = LocateAccesser(context)
        location.setOnMyLocationListener(object: onMyLocationListener {
            override fun onCurrentLocation(context: Context?, location: Location) {}

            override fun onGpsLocation(context: Context?, location: Location?) {
                mapRepository.onGpsLocation(context, location)
                setCarrentLocate(location, LocationType.GPS)
                firstLoadLocation = false
            }

            override fun onNetworkLocation(context: Context?, location: Location?) {
                mapRepository.onNetworkLocation(context, location)
                if (checkNetworkLocation(location)) {
                    setCarrentLocate(location, LocationType.NETWORK)
                    firstLoadLocation = false
                }
            }

            override fun onGpsLogLocation(context: Context?, location: Location?) {
                mapRepository.onGpsLogLocation(context, location)
                setCarrentLocate(location, LocationType.LOG)
            }

            override fun onNetworkLogLocation(context: Context?, location: Location?) {                setCarrentLocate(location, LocationType.LOG)

                if(firstLoadLocation)return
                setCarrentLocate(location, LocationType.LOG)
            }
        })
        location.start()
    }

    fun stopLocation(){
        location.setOnMyLocationListener(null)
        location.stop()
    }

    var prevNetworkLocation: Location? = null
    fun checkNetworkLocation(location: Location?): Boolean {
        if (location == null) return true
        if (prevNetworkLocation == null) {
            prevNetworkLocation = location
            return true
        }
        val time = location.time
        val prevTime = prevNetworkLocation!!.time
        if (prevTime + 60000 > time) {
            // 60秒未満なので更新しない
            return false
        }
        prevNetworkLocation = location
        return true
    }

    fun setCarrentLocate(location: Location?, type: LocationType){
        if (location == null) {
            // 位置情報を取得できなかったから何もしない
            return
        }

        val current = LatLng(location.latitude, location.longitude)
        val time = location.time

        if (prevCurrentLocation == null) {
            prevCurrentLocation = location
        } else {
            val prevCurrent = LatLng(prevCurrentLocation!!.latitude, prevCurrentLocation!!.longitude)
            val prevTime = prevCurrentLocation!!.time

            if (type == LocationType.GPS) {
                // GPS情報は無条件で通す
            } else if (prevTime + 1000 > time) {
                // 1秒未満なので更新しない
                return
            } else if (distanceCalc(current.latitude,current.longitude, prevCurrent.latitude, prevCurrent.longitude) > 1) {
                // 移動範囲が１メートル以内なので更新しない
                return
            }
            prevCurrentLocation = location
        }

        // 方向のデータに現在地を渡す
        sensor?.onLocationChanged(location)

        // mapRepositoryに現在地を渡す
        mapRepository.setLocationCurrent(current)
        routeRepository.setLocationCurrent(current)
        guideRepository.setLocationCurrent(current)
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 方位
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private val magneticSensorListener = object: MagneticSensorListener {
        override fun onChange(orientation: Float) {
            //Log.i("TESTESTEST","orientation:"+orientation)

            //android.util.Log.i("TESTESTEST","angle:" + orientation)
            val status = mapRepository.mapStatusString.value

            if (status == null) {
                return@onChange
            }

            if (status != MapController.Companion.MAP_STATUS.MAP.toString()
                && status != MapController.Companion.MAP_STATUS.MAP_DIRECT_MOVE.toString()
                && status != MapController.Companion.MAP_STATUS.TOUCH_MOVE.toString()) {
                return@onChange
            }

            mapRepository.realTimeAngle.value = orientation

        }
    }
    private var sensor :MagneticSensor? = null

    private fun startSensor(context: Context) {
        if (sensor == null) {
            sensor = MagneticSensor(context, magneticSensorListener)
        }
        sensor?.onResume(context)
    }

    private fun resumeSensor(context: Context) {
        sensor?.onResume(context)
    }

    private fun pauseSensor(context: Context) {
        sensor?.onPause()
    }

    private fun stopSensor() {
        sensor?.onPause()
    }

}