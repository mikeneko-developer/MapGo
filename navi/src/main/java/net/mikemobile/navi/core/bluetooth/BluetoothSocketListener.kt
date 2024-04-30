package net.mikemobile.navi.bluetooth

import android.bluetooth.BluetoothDevice
import net.mikemobile.navi.core.bluetooth.BluetoothData

interface BluetoothSocketListener {

    fun onStatus(type: BluetoothUtil.CONNECT_TYPE)
    fun onConnected(device: BluetoothDevice)
    fun onRead(data: BluetoothData)
    fun onReadHandler(count:Int,max:Int)
    fun onWriteResult(data:BluetoothData, enable: Boolean)
    fun onDisconnect()
}
