package net.mikemobile.navi.core.gps

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
import java.lang.ref.WeakReference
import java.util.Date


interface MagneticSensorListener {
    fun onChange(orientation: Float)
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
    var saveAcceleVal = FloatArray(3)
    var saveMagneticVal = FloatArray(3)

    private val mAccelerometerReading = FloatArray(3) // センサーから加速度の値を受け取る配列
    private val mMagnetometerReading = FloatArray(3) // センサーから地磁気の値を受け取る配列


    private var fOrientation: FloatArray? = null
    private val mOrientationReading = FloatArray(3) // センサーから向きの値を受け取る配列
    private var _matrix: Matrix? = null
    private var _keepValue = 0f//前回の角度

    // ローパスフィルタ，これのおかげで針のブレがかなり減ります！
    private fun lowpassFilter(vecPrev: FloatArray, vecNew: FloatArray):FloatArray {
        val alpha = 0.98f
        for (i in vecNew.indices) {
            vecPrev[i] = alpha * vecPrev[i] + (1 - alpha) * vecNew[i]
        }
        return vecPrev
    }

    //方位センサー関係
    private var manager: SensorManager? = null
    private var isSensorResisted = false

    private var accelerometer: Sensor? = null
    private var magneticField: Sensor? = null

    private val sensorDelay: Int = SensorManager.SENSOR_DELAY_GAME // センサーの値を受け取る間隔

    private var myHandler: MyHandler? = null

    //SurfaceViewを描画するHandler
    private class MyHandler : Handler() {
        var listener: MagneticSensorListener? = null
        fun SetMe(me: WeakReference<MagneticSensor>) {
            listener = me.get()!!.listener
        }

        override fun handleMessage(msg: Message) {
            removeMessages(WHAT)
            if (!isAlive) {
                //フラグオフなら終了
                return
            }

            // orientation
            listener?.onChange(orientation)
        }

        companion object {
            const val WHAT = 1
        }
    }

    fun onResume(context: Context) {

        //センサーの起動
        if (manager == null) {
            manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        }

        if (SENSOR_MODE == MODE_ACCER_MGNE) {// 加速度センサー、磁気センサーを使った取得方法
            accelerometer = manager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            magneticField = manager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

            manager!!.registerListener(this, accelerometer, sensorDelay)
            manager!!.registerListener(this, magneticField, sensorDelay)
        }

        if (SENSOR_MODE == MODE_ORIENTATION) {// 角度取得

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
            //fAccell = lowpassFilter(mAccelerometerReading, event.values.clone())
            fAccell = event.values.clone()

        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            //fMagnetic = lowpassFilter(mMagnetometerReading, event.values.clone())
            fMagnetic = event.values.clone()

        } else if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
            fOrientation = event.values.clone()
        }

        if (SENSOR_MODE == MODE_ORIENTATION) {
            if (fOrientation != null) {
                var value = fOrientation!![0]
                //次回用に角度を保存する

                if (prevOrientaion != value) {
                    prevOrientaion = value

                    orientation = (value + geomagneticRotation)
                    //orientation = (value)

                    //Handlerで針画像の描画
                    myHandler!!.sendEmptyMessage(MyHandler.WHAT)
                }

            }
        }

        if (SENSOR_MODE == MODE_ACCER_MGNE) {
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

                val _orientation = rad2deg(fAttitude[0])

                if (prevOrientaion != _orientation) {
                    prevOrientaion = _orientation

                    orientation = (_orientation + geomagneticRotation)
                    //orientation = (_orientation)

                    //Handlerで針画像の描画
                    myHandler!!.sendEmptyMessage(MyHandler.WHAT)
                }
            }
        }


    }



    private fun rad2deg(rad: Float): Float {
        return rad * 180.0.toFloat() / Math.PI.toFloat()
    }

}