package net.mikemobile.navi.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.util.Log
import net.mikemobile.navi.core.bluetooth.BluetoothData

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class BluetoothSocketThread : Thread {

    private var mmSocket: BluetoothSocket? = null
    private var mmInStream: InputStream? = null
    private var mmOutStream: OutputStream? = null
    private val mHandler = Handler(Looper.getMainLooper())

    private var status: BluetoothUtil.CONNECT_TYPE = BluetoothUtil.CONNECT_TYPE.NONE
    private var listener: BluetoothSocketListener = object : BluetoothSocketListener {
        override fun onReadHandler(count: Int, max: Int) {}
        override fun onStatus(type: BluetoothUtil.CONNECT_TYPE) {}
        override fun onConnected(device: BluetoothDevice) {}
        override fun onRead(data: BluetoothData) {}
        override fun onWriteResult(data:BluetoothData, enable: Boolean) {}
        override fun onDisconnect() {}
    }

    constructor(socket: BluetoothSocket, socketType: BluetoothUtil.SOCKET_TYPE, l: BluetoothSocketListener) {
        listener = l
        setup(socket, socketType)
    }

    constructor(socket: BluetoothSocket, socketType: BluetoothUtil.SOCKET_TYPE) {
        setup(socket, socketType)
    }

    private fun setup(socket: BluetoothSocket, socketType: BluetoothUtil.SOCKET_TYPE) {

        status = BluetoothUtil.CONNECT_TYPE.CONNECTION

        Log.d(TAG, "create ConnectedThread: $socketType")
        mmSocket = socket
        var tmpIn: InputStream? = null
        var tmpOut: OutputStream? = null

        // Get the BluetoothSocket input and output streams
        try {
            tmpIn = socket.inputStream
            tmpOut = socket.outputStream
        } catch (e: IOException) {
            Log.e(TAG, "temp sockets not created", e)

            status = BluetoothUtil.CONNECT_TYPE.ERROR
            mHandler.post(object : CustomRunnable(null) {
                override fun run(obj: Any) {
                    listener.onStatus(BluetoothUtil.CONNECT_TYPE.ERROR)
                }
            })

            return
        }

        mmInStream = tmpIn
        mmOutStream = tmpOut

        status = BluetoothUtil.CONNECT_TYPE.CONNECTED
    }

    override fun run() {

        if (status == BluetoothUtil.CONNECT_TYPE.CONNECT_FAILD) {
            mHandler.post(object : CustomRunnable(null) {
                override fun run(obj: Any) {
                    listener.onStatus(BluetoothUtil.CONNECT_TYPE.CONNECT_FAILD)
                }
            })
            return
        }

        val device = mmSocket!!.remoteDevice

        mHandler.post(object : CustomRunnable(device) {
            override fun run(obj: Any) {
                listener.onStatus(BluetoothUtil.CONNECT_TYPE.CONNECTED)
                listener.onConnected(obj as BluetoothDevice)
            }
        })

        Log.i(TAG, "BEGIN mConnectedThread")
        var readWrite = BluetoothReadWrite()
        readWrite.setOnBluetoothDataResultListener(object: OnBluetoothDataResult {

            override fun onReadByte(param_buffer: ByteArray,buffer: ByteArray, type: BluetoothReadWrite.DATA_TYPE) {
                Log.i(TAG, "onReadByte")

                var data = BluetoothData()
                data.setType(type)

                data.setParamByteData(param_buffer)
                data.setByteData(buffer)

                mHandler.post(object : CustomRunnable(data) {
                    override fun run(obj: Any) {
                        listener.onRead(obj as BluetoothData)
                    }
                })
            }


            override fun onReadHandler(count: Int, max: Int) {
                listener.onReadHandler(count,max)
            }

        })
        // Keep listening to the InputStream while connected
        while (status == BluetoothUtil.CONNECT_TYPE.CONNECTED) {

            var bool = false
            mmInStream?.let{
                bool = readWrite.read(it)
            }

            if(bool){
                status = BluetoothUtil.CONNECT_TYPE.CONNECT_LOST
                listener.onStatus(status)
                break
            }
        }

        if (status != BluetoothUtil.CONNECT_TYPE.CANCEL) {
            status = BluetoothUtil.CONNECT_TYPE.DISCONNECT
        }

        mHandler.post(object : CustomRunnable(null) {
            override fun run(obj: Any) {
                listener.onStatus(BluetoothUtil.CONNECT_TYPE.DISCONNECT)
                listener.onDisconnect()
            }
        })
    }


    /**
     * Write to the connected OutStream.
     *
     * @param buffer The bytes to write
     */
    fun write(data:BluetoothData) {
        Log.w(TAG, "write --------- buffer")

        var readWrite = BluetoothReadWrite()

        data.getByteData()

        try {

            val buffer = data.buffer
            val type = data.type
            val data_size = buffer.size
            val param_size = data.getParamSize()


            //ヘッダーを取得する
            val header_data = readWrite.setHeader(data.code, type, data_size, param_size)

            //実データ（パラメータ込み）を取得する
            var material = readWrite.setParamAndData(data.param,data.buffer)

            //データ送信
            mmOutStream?.let{
                readWrite.write(mmOutStream!!, header_data, material)
            }

        } catch (e: IOException) {
            Log.e(TAG, "Exception during write", e)
            mHandler.post(object : CustomRunnable(data) {
                override fun run(obj: Any) {
                    listener.onWriteResult(obj as BluetoothData, false)
                }
            })
            return
        }
        mHandler.post(object : CustomRunnable(data) {
            override fun run(obj: Any) {
                listener.onWriteResult(obj as BluetoothData, true)
            }
        })



    }

    fun cancel() {
        if (status == BluetoothUtil.CONNECT_TYPE.CONNECTED) {
            status = BluetoothUtil.CONNECT_TYPE.CANCEL
        } else {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }

            mmSocket = null

            mHandler.post(object : CustomRunnable(null) {
                override fun run(obj: Any) {
                    listener.onStatus(BluetoothUtil.CONNECT_TYPE.DISCONNECT)
                    listener.onDisconnect()
                }
            })
        }
    }

    companion object {
        private val TAG = "BluetoothSocketThread"
    }
}
