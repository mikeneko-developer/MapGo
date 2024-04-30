package net.mikemobile.navi.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import net.mikemobile.navi.bluetooth.BluetoothManagerExt
import net.mikemobile.navi.bluetooth.BluetoothSocketListener
import net.mikemobile.navi.bluetooth.BluetoothUtil
import net.mikemobile.navi.core.bluetooth.BluetoothData
import net.mikemobile.navi.core.gps.MyDate
import net.mikemobile.navi.data.robo.RoboData
import net.mikemobile.navi.database.OnRoomDatabaseListener
import net.mikemobile.navi.database.model.RoboModel
import net.mikemobile.sampletimer.database.entity.LocationFavorite
import net.mikemobile.sampletimer.database.entity.Robo

class RoboConnectRepository(var context: Context) {

    enum class STATUS{
        NONE,
        SAY,
        SAY_END
    }

    var say_text = MutableLiveData<String>().apply {
        value = ""
    }

    var deviceAddress: String? = null
    var bluetoothStatus: Boolean = false

    var status = MutableLiveData<STATUS>().apply{value = STATUS.NONE}

    var connectDevice = MutableLiveData<BluetoothDevice>().apply{value = null}

    var bluetoothControl = BluetoothManagerExt(context, object : BluetoothSocketListener {
        override fun onReadHandler(count: Int, max: Int) {

        }

        override fun onWriteResult(data: BluetoothData, enable: Boolean) {
            android.util.Log.i("RoboConnectRepository","onWriteResult() data:" + data.command)
        }

        override fun onStatus(type: BluetoothUtil.CONNECT_TYPE) {
            //Toast.makeText(context, "status:" + type, Toast.LENGTH_SHORT).show()

            if(type == BluetoothUtil.CONNECT_TYPE.CONNECT_LOST){
                bluetoothStatus = false
                connectDevice.postValue(null)
            }
        }

        override fun onConnected(device: BluetoothDevice) {
            Toast.makeText(context, "onConnected", Toast.LENGTH_SHORT).show()
            bluetoothStatus = true

            connectDevice.postValue(device)
        }

        override fun onRead(data: BluetoothData) {
            android.util.Log.i("RoboConnectRepository","onRead() data:" + data.command)

            if (data.command == "SAY_END") {
                status.postValue(STATUS.SAY_END)
                say_text.postValue("")
            }
        }

        override fun onDisconnect() {
            Toast.makeText(context, "onConnected", Toast.LENGTH_SHORT).show()
            bluetoothStatus = false
            connectDevice.postValue(null)
        }

    })

    fun getBluetoothDevice(): MutableList<BluetoothDevice> {
        return bluetoothControl.getRegisteredDevice()
    }

    fun isConnect(): Boolean {
        connectDevice.value?.let {
            return true
        }
        return false
    }

    fun startBluetooth(){
        bluetoothControl.start()
    }

    fun stopBluetooth(){
        bluetoothControl.stop()
    }

    fun connect(device: BluetoothDevice){
        bluetoothControl.connect(device)
    }

    fun connect(address: String) {
        bluetoothControl.connect(address)
    }

    fun disconnect(){
        bluetoothControl.stop()
    }


    var handler: Handler? = null
    var runnable = object: Runnable {
        override fun run() {
            status.postValue(STATUS.SAY_END)
            say_text.postValue("")
        }
    }
    fun sayText(text: String){
        status.postValue(STATUS.SAY)
        var data = BluetoothData()
        data.id = BluetoothData.ID_SAY_TEXT
        data.text = text

        if (!isConnect()) {
            say_text.postValue(text)

            handler?.let {
                it.removeCallbacks(runnable)
            }

            handler = Handler()
            handler?.postDelayed(runnable, 3000)

            return
        }

        android.util.Log.i("!!!!!!!!!!!!","text:" + text)
        status.postValue(STATUS.SAY)
        bluetoothControl.write(data)

    }


    var roboModel = RoboModel(context)

    var roboList = MutableLiveData<MutableList<RoboData>>().apply{value = mutableListOf()}
    fun saveRoboData(data: RoboData) {
        var robo = Robo()
        robo.id = data.id
        robo.name = data.name
        robo.address = data.address
        robo.save_date = MyDate.getTimeMillis()

        roboModel.save(robo, object: OnRoomDatabaseListener {
            override fun onError(error_code: Int) {}
            override fun onDeleted() {}
            override fun onCreated(data: LocationFavorite) {}
            override fun onUpdated(data: LocationFavorite) {}
            override fun onRead(list: List<LocationFavorite>) {}

            override fun onCreated(data: Robo) {}
            override fun onUpdated(data: Robo) {}
            override fun onReadRobo(list: List<Robo>) {}
        })
    }

    fun readRoboList() {
        roboModel.read(object:OnRoomDatabaseListener{
            override fun onError(error_code: Int) {}
            override fun onDeleted() {}
            override fun onCreated(data: LocationFavorite) {}
            override fun onUpdated(data: LocationFavorite) {}
            override fun onRead(list: List<LocationFavorite>) {}

            override fun onCreated(data: Robo) {}
            override fun onUpdated(data: Robo) {}
            override fun onReadRobo(list: List<Robo>) {
                var rList = mutableListOf<RoboData>()
                list.forEach{
                    rList.add(RoboData(it.id, it.name, it.address))
                }
                roboList.postValue(rList)
            }
        })
    }
}