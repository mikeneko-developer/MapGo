/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mikemobile.navi.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import android.view.View
import net.mikemobile.navi.R
import net.mikemobile.navi.core.bluetooth.BluetoothData

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
class BluetoothManagerExt
/**
 * Constructor. Prepares a new BluetoothChat session.
 *
 * @param context The UI Activity Context
 */
    (private val context: Context, private val listener: BluetoothSocketListener) : BluetoothSocketListener {


    companion object {
        // Debugging
        private val TAG = "BluetoothManagerExt"
    }

    // Member fields
    private val mAdapter: BluetoothAdapter

    private var clientThread: ClientThread? = null


    init {
        mAdapter = BluetoothAdapter.getDefaultAdapter()
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    @Synchronized
    fun start() {
        Log.d(TAG, "start")

    }




    @Synchronized
    fun connect(address: String) {
        mAdapter?.let{
            val device = it.getRemoteDevice(address)
            connect(device)
        }

    }


    @Synchronized
    fun connect(device: BluetoothDevice) {
        Log.d(TAG, "connect to: $device")

        // Start the thread to listen on a BluetoothServerSocket
        if (clientThread == null) {
            clientThread = ClientThread(device, mAdapter, this)
            clientThread!!.start()
        }
    }


    @Synchronized
    fun stop() {
        if (clientThread != null) {
            clientThread!!.cancel()
            clientThread = null
        }
    }


    override fun onStatus(type: BluetoothUtil.CONNECT_TYPE) {
        Log.i(TAG, "onStatus : $type")
        listener.onStatus(type)
    }

    override fun onConnected(device: BluetoothDevice) {
        Log.i(TAG, "onConnected : " + device.name)
        listener.onConnected(device)

    }

    override fun onRead(data: BluetoothData) {
        Log.i(TAG, "onRead")
        listener.onRead(data)
    }

    override fun onReadHandler(count: Int, max: Int) {
        listener.onReadHandler(count,max)
    }

    override fun onWriteResult(data:BluetoothData, enable: Boolean) {
        Log.i(TAG, "onWriteResult")
        listener.onWriteResult(data,enable)

    }

    override fun onDisconnect() {
        Log.i(TAG, "onDisconnect")
        listener.onDisconnect()

    }

    fun write(data:BluetoothData) {
        if (clientThread != null) {
            clientThread!!.write(data)
        }
    }


    var deviceList = mutableListOf<BluetoothDevice>()
    fun getRegisteredDevice(): MutableList<BluetoothDevice>{
        var deviceList = mutableListOf<BluetoothDevice>()

        // Get a set of currently paired devices
        // Get a set of currently paired devices
        val pairedDevices: Set<BluetoothDevice> = mAdapter.getBondedDevices()

        // If there are paired devices, add each one to the ArrayAdapter
        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size > 0) {
            for (device in pairedDevices) {
                deviceList.add(device)
            }
        }
        return deviceList
    }

    fun stopSearchDevice(){
        // Make sure we're not doing discovery anymore
        // Make sure we're not doing discovery anymore
        if (mAdapter != null) {
            mAdapter.cancelDiscovery()
        }
    }
}
