package com.sp.app.maplib.gps

import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.sp.app.maplib.Constant
import com.sp.app.maplib.service.AppService
import com.sp.app.maplib.util.MyDate
import com.sp.app.maplib.util.directionCalc
import com.sp.app.maplib.util.distanceCalc
import java.util.*


class LocateAccesser(var context: Context, var power_type: POWER_TYPE = POWER_TYPE.MIDDLE) {

    companion object {
        const val TAG = "LocateAccesser"

        enum class POWER_TYPE {
            HIGH,
            MIDDLE,
            LOW
        }

        enum class STATUS {
            STOP,
            START
        }

        enum class TYPE {
            GPS,
            NETWORK,
            LOG_GPS,
            LOG_NETWORK,
        }

        const val WAIT_1000 = 1000L
        const val WAIT_500 = 500L

        const val METRE_1 = 1f
        const val METRE_2 = 2f

        const val UPDATE_TIME = WAIT_500
    }
    init{
        setUp()
    }

    var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)


    lateinit var mLocationManager: LocationManager
    private var criteria = Criteria()
    private var status = STATUS.STOP

    /** ================== インターフェース ==================  */
    private var listener: onMyLocationListener? =
        SimpleLocationListener()
    private val gpslistener: LocationListener = object : LocationListener {
        override fun onProviderDisabled(provider: String) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onLocationChanged(location: Location) {
            Log.v(TAG, "GPS情報取得 onLocationChanged")
            Log.i(TAG, "緯度 Latitude:" + location.latitude.toString())
            Log.i(TAG, "経度 Longitude" + location.longitude.toString())
            Log.i(TAG, "エラーレンジ Accuracy" + location.accuracy.toString())
            Log.i(TAG, "標高 Altitude:" + location.altitude.toString())
            Log.i(TAG, "Time:" + location.time.toString())
            Log.i(TAG, "Speed:" + location.speed.toString())
            //Log.i(TAG, "Bearing:" + location.bearing.toString())

            throwLocation(TYPE.GPS, location)
        }
    }


    @SuppressLint("WrongConstant")
    private fun setUp(){
        var locationRequest = LocationRequest()
        locationRequest.setPriority(
            // どれにするかはお好みで、ただしできない状況ではできないので
            LocationRequest.PRIORITY_HIGH_ACCURACY)
            //LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
            //LocationRequest.PRIORITY_LOW_POWER)
            //LocationRequest.PRIORITY_NO_POWER)


        mLocationManager = context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if(checkLocationEnabled()){
            android.util.Log.i(TAG,"" + checkLocationEnabled())
            //return
        }

        val criteria = Criteria()
        criteria.setAltitudeRequired(false)
        criteria.setSpeedRequired(false)
        criteria.setCostAllowed(false) // 基地局からの位置情報精度（通信料が余計に発生する）
        criteria.setBearingRequired(false)

        //API level 9 and up
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_FINE)
        criteria.setVerticalAccuracy(Criteria.ACCURACY_FINE)

        // Accuracyを設定
        var setup_accuracy_high = true

        /**
        if(setup_accuracy_high) {
            try {
                criteria.setAccuracy(Criteria.ACCURACY_HIGH)
                android.util.Log.i(TAG, "accuracy:Criteria.ACCURACY_HIGH")
                setup_accuracy_high = false
            } catch (e: Exception) {
                android.util.Log.e(TAG, "GPSの精度設定失敗 accuracy:Criteria.ACCURACY_HIGH")
                android.util.Log.e(TAG, e.toString())
            }
        }
        */
        if(setup_accuracy_high) {
            try {
                criteria.setAccuracy(Criteria.ACCURACY_FINE)
                android.util.Log.i(TAG, "accuracy:Criteria.ACCURACY_FINE")
                setup_accuracy_high = false
            } catch (e: Exception) {
                android.util.Log.e(TAG, "GPSの精度設定失敗 accuracy:Criteria.ACCURACY_FINE")
                android.util.Log.e(TAG, e.toString())
            }
        }
        if(setup_accuracy_high) {
            try {
                criteria.setAccuracy(Criteria.ACCURACY_MEDIUM)
                android.util.Log.i(TAG, "accuracy:Criteria.ACCURACY_MEDIUM")
                setup_accuracy_high = false
            } catch (e: Exception) {
                android.util.Log.e(TAG, "GPSの精度設定失敗 accuracy:Criteria.ACCURACY_MEDIUM")
                android.util.Log.e(TAG, e.toString())
            }
        }
        if(setup_accuracy_high) {
            try {
                criteria.setAccuracy(Criteria.ACCURACY_COARSE)
                android.util.Log.i(TAG, "accuracy:Criteria.ACCURACY_COARSE")
                setup_accuracy_high = false
            } catch (e: Exception) {
                android.util.Log.e(TAG, "GPSの精度設定失敗 accuracy:Criteria.ACCURACY_COARSE")
                android.util.Log.e(TAG, e.toString())
            }
        }
        if(setup_accuracy_high) {
            try {
                criteria.setAccuracy(Criteria.ACCURACY_LOW)
                android.util.Log.i(TAG, "accuracy:Criteria.ACCURACY_LOW")
            } catch (e: Exception) {
                android.util.Log.e(TAG, "GPSの精度設定失敗 accuracy:Criteria.ACCURACY_LOW")
                android.util.Log.e(TAG, e.toString())
            }
        }

        try{
            if (power_type == POWER_TYPE.HIGH) {
                criteria.powerRequirement = Criteria.POWER_HIGH // PowerRequirementを指定(高消費電力)
            } else if (power_type == POWER_TYPE.MIDDLE) {
                criteria.powerRequirement = Criteria.POWER_MEDIUM // PowerRequirementを指定(中消費電力)
            } else {
                criteria.powerRequirement = Criteria.POWER_LOW // PowerRequirementを指定(低消費電力)
            }
        }catch(e: Exception){
            Log.e(TAG, "GPSのバッテリー消費モード設定失敗 power_type:" + power_type)
            Log.e(TAG, e.toString())
        }

        this.criteria = criteria
    }

    fun setOnMyLocationListener(l: onMyLocationListener?) {
        listener = l
    }

    fun checkLocationEnabled(): Boolean {
        val provider = LocationManager.NETWORK_PROVIDER
        //val provider = LocationManager.GPS_PROVIDER;
        //val provider = LocationManager.PASSIVE_PROVIDER;
        //val provider = LocationManager.GPS_PROVIDER;
        //val provider = LocationManager.PASSIVE_PROVIDER;
        return mLocationManager.isProviderEnabled(provider)
    }


    @SuppressLint("MissingPermission")
    fun start() {
        if(!checkLocationEnabled()){ return }
        if(status == STATUS.START){ return }
        status =
            STATUS.START


        if(true){
            //mLocationManager.requestLocationUpdates(WAIT_1000, 1f, criteria, gpslistener, null)
        }

        if(true) {

            //利用可能
            //使える中で最も条件にヒットする位置情報サービスを取得する
            val bestProvider_ = mLocationManager.getBestProvider(criteria, true)

            if (bestProvider_ != null) { //位置更新の際のリスナーを登録。省電力のために通知の制限をする。
                //最小で15000msec周期、最小で1mの位置変化の場合(つまり、どんなに変化しても15000msecのより短い間隔では通知されず、1mより小さい変化の場合は通知されない。)
                mLocationManager.requestLocationUpdates(bestProvider_, UPDATE_TIME, METRE_1, gpslistener)
            }
        }

        startTimer()
    }

    fun stop() {
        if(status == STATUS.STOP){ return }
        status =
            STATUS.STOP

        mLocationManager.removeUpdates(gpslistener)

        stopTimer()
    }



    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private var handler = Handler()
    lateinit var timerTask: TimerTask
    lateinit var timer: Timer
    private var log_gps_lat:Double = 0.0
    private var log_gps_lon:Double = 0.0
    private var log_network_lat:Double = 0.0
    private var log_network_lon:Double = 0.0

    private var network_lat:Double = 0.0
    private var network_lon:Double = 0.0

    private var log_gps_locate:Location? = null
    private var log_network_locate:Location? = null
    private var network_locate:Location? = null

    private val WAIT_TIME_10SECOND = 10 * 1000

    private fun startTimer() {
        timer = Timer()
        timer = Timer(true)

        timerTask = object : TimerTask() {
            override fun run() {
                handler.post(object:Runnable {
                    override fun run() {
                        getLogLocation()
                    }
                })
            }
        }

        //delay = Global.INTERVAL_PERIOD;
        //int delay = 1000 * 60 * 60 * 24;
        /** ▼ 2014/3/21 追加 ===============================================  */ //通信用データに問題がある場合は処理を実行しないよう追加
        /** ▲ =============================================================  */ //delay = Global.INTERVAL_PERIOD;
        val now = Date()

        timer.scheduleAtFixedRate(timerTask, now,
            UPDATE_TIME
        ) // 開始時間を基準にして指定時間ごとに繰り返し
        //timer.schedule(timerTask, wait_long);
    }

    private fun stopTimer() {
        timerTask.cancel()
    }

    @SuppressLint("MissingPermission")
    private fun getLogLocation(){
        //Log.v(TAG, "getLogLocation >>>>>>>>>>>>>>>>>>>")
        var locate: Location? = null

        //　無線測位で取得してみる
        locate = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        if (locate != null) { // 現在地情報取得成功

            if(log_network_locate == null || log_network_locate!!.time + WAIT_TIME_10SECOND <= locate.time) {

                val log_lat = locate.latitude
                val log_lon = locate.longitude
                if (log_lat != log_network_lat || log_lon != log_network_lon) {
                    log_network_lat = log_lat
                    log_network_lon = log_lon
                    log_network_locate = locate

                    Log.v(TAG, "NetworkLogから情報取得")
                    throwLocation(TYPE.LOG_NETWORK, locate)
                } else {
                    //Log.e(TAG, "NetworkLog 位置情報が前回と同じです");
                }
            }
        } else { /* 無線測位取得失敗処理 */
            //Log.e(MyLocation.TAG, "Log 位置情報取得に失敗しました。")
        }


        locate = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (locate != null) { // 現在地情報取得成功

            if(log_gps_locate == null || log_gps_locate!!.time + WAIT_TIME_10SECOND <= locate.time) {
                // 緯度の取得
                val log_lat = locate.latitude
                val log_lon = locate.longitude
                if (log_lat != log_gps_lat || log_lon != log_gps_lon) {
                    log_gps_lat = log_lat
                    log_gps_lon = log_lon
                    log_gps_locate = locate

                    Log.v(TAG, "GPSLogから情報取得")
                    throwLocation(TYPE.LOG_GPS, locate)
                } else {
                    //Log.e(TAG, "GPSLog 位置情報が前回と同じです");
                }
            }


        } else { /* 現在地情報取得失敗処理 */
            //Log.e(MyLocation.TAG, "Log 位置情報取得に失敗しました。")
        }

        //基地局情報取得
        fusedLocationClient.lastLocation.addOnCompleteListener(object: OnCompleteListener<Location?> {
            override fun onComplete(task: Task<Location?>) {
                if (task.isSuccessful && task.result != null) {
                    var location = task.result

                    if (location != null) { // 現在地情報取得成功
                        if(network_locate == null || network_locate!!.time + WAIT_TIME_10SECOND <= location.time){

                            // 緯度の取得
                            val log_lat = location.latitude
                            val log_lon = location.longitude
                            if (log_lat != network_lat || log_lon != network_lon) {
                                network_lat = log_lat
                                network_lon = log_lon
                                network_locate = location


                                Log.v(TAG, "Networkから情報取得")
                                throwLocation(TYPE.NETWORK, location)
                            } else {
                                //Log.e(TAG, "Network1 位置情報が前回と同じです");
                            }

                        }else {
                            // 緯度の取得
                            val log_lat = location.latitude
                            val log_lon = location.longitude
                            if (log_lat != network_lat || log_lon != network_lon) {
                                network_lat = log_lat
                                network_lon = log_lon
                                network_locate = location


                                Log.v(TAG, "Networkから情報取得")
                                throwLocation(TYPE.NETWORK, location)
                            } else {
                                //Log.e(TAG, "Network2 位置情報が前回と同じです");
                            }
                        }

                    } else { /* 現在地情報取得失敗処理 */
                        //Log.e(MyLocation.TAG, "Log 位置情報取得に失敗しました。")
                    }
                } else {
                    Log.d("debug", "計測不能")
                }
            }
        })
    }

    /**
     * 取得した位置情報をlistenerで返す
     */
    private var prevLocation: Location? = null
    private var waitLocation: Location? = null
    private var prevDirection = -1f
    private val STEEP_ANGLE_THRESHOLD = 45f // 急な角度と判定する境界線
    private val STEEP_ANGLE_THRESHOLD2 = 60f // 急な角度と判定する境界線

    private fun throwLocation(type: TYPE, location: Location) {

        when (type) {
            TYPE.GPS -> {
                listener?.onGpsLocation(location)
            }
            TYPE.NETWORK -> {
                listener?.onNetworkLocation(location)
            }
            TYPE.LOG_GPS -> {
                listener?.onGpsLogLocation(location)
            }
            TYPE.LOG_NETWORK -> {
                listener?.onNetworkLogLocation(location)
            }
        }

        when (type) {
            TYPE.GPS, TYPE.NETWORK, TYPE.LOG_NETWORK -> {
                if (prevLocation == null) {
                    prevLocation = location
                    listener?.onCurrentLocation(location, 0f)

                    Log.v(TAG, "情報を更新")
                } else {

                    // 急な方向転換が正しい動きか判定する
                    if (waitLocation != null) {

                        if (waitLocation!!.time + (UPDATE_TIME * 2) > location.time) {
                            val direction = directionCalc(
                                waitLocation!!.latitude,
                                waitLocation!!.longitude,
                                location.latitude,
                                location.longitude,
                            )

                            if (prevDirection != -1f && (prevDirection - direction) > STEEP_ANGLE_THRESHOLD2
                                || prevDirection != -1f && (direction - prevDirection) > STEEP_ANGLE_THRESHOLD2) {
                                // また変な方向を向いたのでこのデータは破棄とする
                                waitLocation = null
                                return
                            } else {
                                // waitLocationのデータは正しいデータとして扱うと決まったので値を送信した上で通常の処理へ進める

                                // 1点目から2点目へ向く角度を方位として0-360のFloat型で取得
                                val direction = directionCalc(
                                    prevLocation!!.latitude,
                                    prevLocation!!.longitude,
                                    waitLocation!!.latitude,
                                    waitLocation!!.longitude,
                                )

                                listener?.onCurrentLocation(waitLocation!!, direction)

                                prevLocation = waitLocation
                            }
                        } else {
                            // waitLocationを保持したまま（UPDATE_TIME * 2)以上時間が経過したら破棄する
                            waitLocation = null
                        }
                    }



                    val prevTime = prevLocation!!.time

                    val move = distanceCalc(
                        prevLocation!!.latitude,
                        prevLocation!!.longitude,
                        location.latitude,
                        location.longitude,
                        true
                    )

                    var compulsionUpdate = false
                    if (type == TYPE.GPS) {
                        // GPS情報は無条件で通す
                    } else if (prevTime + (UPDATE_TIME * 6) < MyDate.getTimeMillis()) {
                        // 前のデータからUPDATE_TIMEの２倍以上の時間が経過していたら、問答無用で更新対象
                        Log.i(TAG, "前のデータから"+(UPDATE_TIME * 6)+"以上の時間が経過していたら、問答無用で更新対象" )
                        compulsionUpdate = true
                    } else if (prevTime + UPDATE_TIME > location.time && move < 0.3f) {
                        // 指定時間を経過していないので更新しない かつ、移動範囲が0.3メートル以内なので更新しない
                        Log.e(TAG, "移動範囲が0.3メートル以内なので更新しない" + move)
                        return
                    }

                    // 1点目から2点目へ向く角度を方位として0-360のFloat型で取得
                    val direction = directionCalc(
                        prevLocation!!.latitude,
                        prevLocation!!.longitude,
                        location.latitude,
                        location.longitude,
                    )

                    if (compulsionUpdate) {
                        // 強制的に更新させる
                    } else if (prevDirection != -1f && (prevDirection - direction) > STEEP_ANGLE_THRESHOLD
                        || prevDirection != -1f && (direction - prevDirection) > STEEP_ANGLE_THRESHOLD) {
                        // 急に変な方向を向いたので、一旦保留とする
                        waitLocation = location
                        Log.e(TAG, "急に変な方向を向いたので、一旦保留とする")
                        return
                    }


                    prevLocation = location

                    listener?.onCurrentLocation(location, direction)

                    Log.v(TAG, "情報を更新")
                }
            }
            else -> {}
        }
    }
}