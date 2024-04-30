package com.sp.app.maplib.util

import android.content.Context
import android.graphics.Matrix
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Handler
import android.os.Message
import com.google.android.gms.location.LocationServices
import java.lang.ref.WeakReference
import java.util.Date


interface MagneticSensorListener {
    fun onChangeOrientation(orientation: Float)
    fun onChangeMagnetic(orientation: Float)
}


class MagneticSensor(val context: Context, val listener: MagneticSensorListener) : SensorEventListener {


    companion object {
        const val MODE_ALL = 0
        const val MODE_ORIENTATION = 1
        const val MODE_ACCER_MGNE = 2

        const val SENSOR_MODE = MODE_ORIENTATION

        private var orientation = 0f
        private var prevOrientaion = 0f
        private var geomagneticRotation = 0f
        private var isAlive = false

        private var magneticDirection = 0f
        private var prevMagneticDirection = 0f

        fun toOrientationString(angle: Float): String? {
            var orientationName = "北　"

            if (angle <= -158) {
                orientationName = "南　"
            } else if (angle <= -113) {
                orientationName = "南西"
            } else if (angle <= -68) {
                orientationName = "西　"
            } else if (angle <= -23) {
                orientationName = "北西"
            } else if (angle <= 23) {
                orientationName = "北　"
            } else if (angle <= 68) {
                orientationName = "北東"
            } else if (angle <= 113) {
                orientationName = "東　"
            } else if (angle <= 158) {
                orientationName = "南東"
            } else if (angle <= 203) {
                orientationName = "南　"
            } else if (angle <= 258) {
                orientationName = "南西"
            } else if (angle <= 293) {
                orientationName = "西　"
            } else if (angle >= 338) {
                orientationName = "北西"
            }

            return orientationName
        }
    }

    private var fAccell: FloatArray? = null
    private var fMagnetic: FloatArray? = null
    private var fOrientation: FloatArray? = null
    private var _matrix: Matrix? = null

    //方位センサー関係
    private var manager: SensorManager? = null
    private var isSensorResisted = false

    private var accelerometer: Sensor? = null
    private var magneticField: Sensor? = null
    private val mAccelerometerReading = FloatArray(3) // センサーから加速度の値を受け取る配列
    private val mMagnetometerReading = FloatArray(3) // センサーから地磁気の値を受け取る配列

    private val sensorDelay: Int = SensorManager.SENSOR_DELAY_GAME // センサーの値を受け取る間隔

    private var myHandler: MyHandler? = null

    //SurfaceViewを描画するHandler
    private class MyHandler : Handler() {
        var listener: MagneticSensorListener? = null
        fun SetMe(me: WeakReference<MagneticSensor>) {
            listener = me.get()!!.listener
        }

        override fun handleMessage(msg: Message) {

            if (!isAlive) {
                //フラグオフなら終了
                return
            }

            //android.util.Log.i("TESTESTEST","msg.obj :" + msg.obj)

            if (msg.what == WHAT) {
                removeMessages(WHAT)
            } else if (msg.what == ORIENTATION) {
                removeMessages(ORIENTATION)

                // orientation
                listener?.onChangeOrientation(orientation)
            } else if (msg.what == MANETIC) {
                removeMessages(MANETIC)

                // センサー
                listener?.onChangeMagnetic(magneticDirection)
            }
        }

        companion object {
            const val WHAT = 1
            const val ORIENTATION = 2
            const val MANETIC = 3
        }
    }

    fun onResume(context: Context) {

        //センサーの起動
        if (manager == null) {
            manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        }

        if (SENSOR_MODE == MODE_ALL || SENSOR_MODE == MODE_ACCER_MGNE) {// 加速度センサー、磁気センサーを使った取得方法
            accelerometer = manager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            magneticField = manager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

            manager!!.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            manager!!.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL)
        }

        if (SENSOR_MODE == MODE_ALL || SENSOR_MODE == MODE_ORIENTATION) {// 角度取得

            val list = manager!!.getSensorList(Sensor.TYPE_ORIENTATION)
            //取得に成功した場合のみ処理
            if (list.size > 0){
                //リスナーに登録
                // 第１引数 : Listener
                // 第２引数 : センサー指定（リストの最初がOrientation）
                // 第３引数 : 感度
                manager!!.registerListener(this, list[0], sensorDelay)
            }

            _matrix = Matrix()
        }

        isSensorResisted = true

        //ループフラグ・オン
        isAlive = true

        //Handlerを起動
        myHandler = MyHandler()
        myHandler!!.SetMe(WeakReference(this))

    }

    fun onPause() {
        //センサーの終了
        if (isSensorResisted) {
            manager!!.unregisterListener(this)
            isSensorResisted = false

            //ループフラグ・オフ
            isAlive = false

            //Handlerにメッセージが残っていれば削除
            myHandler!!.removeMessages(MyHandler.WHAT)
            myHandler!!.removeMessages(MyHandler.ORIENTATION)
            myHandler!!.removeMessages(MyHandler.MANETIC)

        }
    }

    fun onLocationChanged(location: Location) {
        val latitude: Float = location.latitude.toFloat()
        val longitude: Float = location.longitude.toFloat()
        val altitude: Float = location.altitude.toFloat()

        val geomagnetic = GeomagneticField(latitude, longitude, altitude, Date().getTime())
        geomagneticRotation = geomagnetic.declination


    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            fAccell = lowpassFilter(mAccelerometerReading, event.values.clone())
            fAccell = event.values.clone()

        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            //fMagnetic = lowpassFilter(mMagnetometerReading, event.values.clone())
            fMagnetic = event.values.clone()

        } else if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
            fOrientation = event.values.clone()
        }

        if (SENSOR_MODE == MODE_ALL && event.sensor.type == Sensor.TYPE_ORIENTATION
            || SENSOR_MODE == MODE_ORIENTATION) {
            if (fOrientation != null) {
                var value = fOrientation!![0]
                //次回用に角度を保存する

                if (prevOrientaion != value) {
                    prevOrientaion = value

                    orientation = (value + geomagneticRotation)
                    //orientation = (value)

                    //Handlerで針画像の描画
                    myHandler!!.sendEmptyMessage(MyHandler.ORIENTATION)
                }

            }
        }


        if (SENSOR_MODE == MODE_ALL && event.sensor.type != Sensor.TYPE_ORIENTATION
            || SENSOR_MODE == MODE_ACCER_MGNE) {
            if (fAccell != null && fMagnetic != null) {
                val inR = FloatArray(9)

                SensorManager.getRotationMatrix(inR, null, fAccell, fMagnetic)

                val outR = FloatArray(9)
                SensorManager.remapCoordinateSystem(
                    inR,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Y,
                    outR
                )
                val fAttitude = FloatArray(3)
                SensorManager.getOrientation(outR, fAttitude)

                val _magneticDirection = rad2deg(fAttitude[0])

                if (prevMagneticDirection != _magneticDirection) {
                    prevMagneticDirection = _magneticDirection

                    magneticDirection = (_magneticDirection + geomagneticRotation)
                    //orientation = (_orientation)

                    //Handlerで針画像の描画
                    myHandler!!.sendEmptyMessage(MyHandler.MANETIC)
                }
            }
        }
    }

    private fun rad2deg(rad: Float): Float {
        return rad * 180.0.toFloat() / Math.PI.toFloat()
    }

    // ローパスフィルタ，これのおかげで針のブレがかなり減ります！
    private fun lowpassFilter(vecPrev: FloatArray, vecNew: FloatArray):FloatArray {
        val alpha = 0.98f
        for (i in vecNew.indices) {
            vecPrev[i] = alpha * vecPrev[i] + (1 - alpha) * vecNew[i]
        }
        return vecPrev
    }

}