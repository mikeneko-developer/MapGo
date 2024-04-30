package com.sp.app.maplib.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.sp.app.maplib.MapController
import com.sp.app.maplib.gps.LocateAccesser
import com.sp.app.maplib.gps.onMyLocationListener
import com.sp.app.maplib.notification.MyNotification
import com.sp.app.maplib.repository.MapRepository
import com.sp.app.maplib.util.MagneticSensor
import com.sp.app.maplib.util.MagneticSensorListener
import org.koin.android.ext.android.inject


/**
 * GPSおよび方位情報を取得し、各Repositoryに返す処理
 */


interface OnServiceListener{
    fun onUndindService()
}

class AppService : Service() {

    companion object {
        const val TAG = "AppService"
    }

    private val notification = MyNotification()

    private val mapRepository: MapRepository by inject()

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
    // 位置情報
    ////////////////////////////////////////////////////////////////////////////////////////////////
    lateinit var location: LocateAccesser

    var firstLoadLocation = false

    private fun startLocation(context: Context){
        firstLoadLocation = true
        location = LocateAccesser(context)
        location.setOnMyLocationListener(object: onMyLocationListener {
            override fun onCurrentLocation(location: Location, direction: Float) {
                mapRepository.onCurrentLocation(location, direction)

                // 方向のデータに現在地を渡す
                sensor?.onLocationChanged(location)
                firstLoadLocation = false

            }

            override fun onGpsLocation(location: Location?) {
                mapRepository.onGpsLocation(location)
            }

            override fun onNetworkLocation(location: Location?) {
                mapRepository.onNetworkLocation(location)
            }

            override fun onGpsLogLocation(location: Location?) {
                mapRepository.onGpsLogLocation(location)
            }

            override fun onNetworkLogLocation(location: Location?) {
                mapRepository.onNetworkLogLocation(location)
            }
        })
        location.start()
    }

    private fun stopLocation(){
        location.setOnMyLocationListener(null)
        location.stop()
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 方位
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private val sensorListener = object: MagneticSensorListener {
        override fun onChangeOrientation(orientation: Float) {
            val status = mapRepository.mapStatusString.value

            if (status == null || status == MapController.Companion.MAP_STATUS.MAP_LOAD.toString()) {
                return
            }

            mapRepository.deviceOrientation.value = orientation
        }

        override fun onChangeMagnetic(orientation: Float) {
            val status = mapRepository.mapStatusString.value

            if (status == null || status == MapController.Companion.MAP_STATUS.MAP_LOAD.toString()) {
                return
            }

            mapRepository.magneticDirection.value = orientation

        }
    }
    private var sensor: MagneticSensor? = null

    private fun startSensor(context: Context) {
        if (sensor == null) {
            sensor = MagneticSensor(context, sensorListener)
            sensor?.onResume(context)
        } else {
            sensor?.onResume(context)
        }
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 目的地
    ////////////////////////////////////////////////////////////////////////////////////////////////
}