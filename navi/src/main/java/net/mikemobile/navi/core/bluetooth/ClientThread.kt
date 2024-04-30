package net.mikemobile.navi.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log

import java.io.IOException

import net.mikemobile.navi.bluetooth.BluetoothUtil.MY_UUID_INSECURE
import net.mikemobile.navi.bluetooth.BluetoothUtil.MY_UUID_SECURE
import net.mikemobile.navi.core.bluetooth.BluetoothData

/**
 * This thread runs while attempting to make an outgoing connection
 * with a device. It runs straight through; the connection either
 * succeeds or fails.
 */
class ClientThread : Thread {

    private var mmSocket: BluetoothSocket? = null
    private var mSocketType: BluetoothUtil.SOCKET_TYPE? = null
    private var mAdapter: BluetoothAdapter? = null
    private var status: BluetoothUtil.CONNECT_TYPE = BluetoothUtil.CONNECT_TYPE.NONE

    private val mHandler = Handler()
    private var socketThread: BluetoothSocketThread? = null

    private var listener: BluetoothSocketListener = object : BluetoothSocketListener {
        override fun onReadHandler(count: Int, max: Int) {}
        override fun onStatus(type: BluetoothUtil.CONNECT_TYPE) {}
        override fun onConnected(device: BluetoothDevice) {}
        override fun onRead(data: BluetoothData) {}
        override fun onWriteResult(data: BluetoothData, enable: Boolean) {}
        override fun onDisconnect() {}
    }


    constructor(device: BluetoothDevice, adapter: BluetoothAdapter, l: BluetoothSocketListener) {
        listener = l
        setup(device, adapter)
    }

    constructor(device: BluetoothDevice, adapter: BluetoothAdapter) {
        setup(device, adapter)
    }

    private fun setup(device: BluetoothDevice, adapter: BluetoothAdapter) {
        var tmp: BluetoothSocket? = null
        var type: BluetoothUtil.SOCKET_TYPE = BluetoothUtil.SOCKET_TYPE.NONE

        status = BluetoothUtil.CONNECT_TYPE.NONE

        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE)
            type = BluetoothUtil.SOCKET_TYPE.SECURE
        } catch (e: IOException) {
            Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e)
            tmp = null
        }



        if (tmp == null) {
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE)
                type = BluetoothUtil.SOCKET_TYPE.INSECURE
            } catch (e: IOException) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e)
                tmp = null
            }

        }



        if (tmp == null) {
            status = BluetoothUtil.CONNECT_TYPE.NONE

            mHandler.post(object : CustomRunnable(null) {
                override fun run(obj: Any) {
                    listener.onStatus(BluetoothUtil.CONNECT_TYPE.ERROR)
                }
            })
            return
        }

        mmSocket = tmp
        mSocketType = type
        mAdapter = adapter


        mHandler.post(object : CustomRunnable(null) {
            override fun run(obj: Any) {
                listener.onStatus(BluetoothUtil.CONNECT_TYPE.CREATE_SOCKET)
            }
        })

    }

    override fun run() {
        Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType!!)

        // Always cancel discovery because it will slow down a connection
        mAdapter!!.cancelDiscovery()

        // Make a connection to the BluetoothSocket
        try {
            // This is a blocking call and will only return on a
            // successful connection or an exception
            mmSocket!!.connect()
        } catch (e: IOException) {
            // Close the socket
            try {
                mmSocket!!.close()
            } catch (e2: IOException) {
                Log.e(
                    TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2
                )
            }

            mHandler.post(object : CustomRunnable(null) {
                override fun run(obj: Any) {
                    listener.onStatus(BluetoothUtil.CONNECT_TYPE.CONNECT_FAILD)
                }
            })

            return
        }



        mHandler.post(object : CustomRunnable(null) {
            override fun run(obj: Any) {
                listener.onStatus(BluetoothUtil.CONNECT_TYPE.CONNECTION)
            }
        })
        synchronized(this@ClientThread) {
            // Start the connected thread
            connected(mmSocket!!, mSocketType)
        }


    }

    fun cancel() {
        try {
            mmSocket!!.close()
        } catch (e: IOException) {
            Log.e(TAG, "close() of connect $mSocketType socket failed", e)
        }

    }


    @Synchronized
    fun connected(socket: BluetoothSocket, socketType: BluetoothUtil.SOCKET_TYPE?) {
        Log.d(TAG, "connected, Socket Type:" + socketType!!)


        if (socketThread != null) {
            socketThread!!.cancel()
            socketThread = null
        }

        // Start the thread to manage the connection and perform transmissions
        socketThread = BluetoothSocketThread(socket, socketType, listener)
        socketThread!!.start()

    }

    fun write(data: BluetoothData) {
        if (socketThread != null) {
            socketThread!!.write(data)
        }
    }

    companion object {
        private val TAG = "ClientThread"
    }
}