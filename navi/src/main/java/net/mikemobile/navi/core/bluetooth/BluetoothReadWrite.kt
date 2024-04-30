package net.mikemobile.navi.bluetooth

import android.annotation.SuppressLint
import android.util.Log

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream





interface OnBluetoothDataResult {
    fun onReadByte(param_buffer: ByteArray, buffer: ByteArray, type: BluetoothReadWrite.DATA_TYPE)
    fun onReadHandler(count:Int,max:Int)
}

class BluetoothReadWrite {

    companion object {

        @JvmField
        open var START_CODE:ByteArray = "START_CODE".toByteArray()

        //lateinit var START_CODE: ByteArray
        private val TAG = "BluetoothReadWrite"

        
        private val CODE_COUNT = 10
        private val TYPE_COUNT = 10
        private val SIZE_COUNT = 10
        private val PARAM_SIZE_COUNT = 10

        private val START_POSITION_CODE = 0
        private val START_POSITION_TYPE = CODE_COUNT
        private val START_POSITION_SIZE = CODE_COUNT + TYPE_COUNT
        private val START_POSITION_PARAM_SIZE = CODE_COUNT + TYPE_COUNT + SIZE_COUNT
        private val START_POSITION_PARAM = CODE_COUNT + TYPE_COUNT + SIZE_COUNT + PARAM_SIZE_COUNT

        // 開始・終了を判定するキー


        private val ONE_BYTE = 512
    }


    // データのタイプ（バイト配列）
    open var TYPE_TEXT = "TEXTS".toByteArray()
    open var TYPE_IMAGE = "IMAGE".toByteArray()
    open var TYPE_MOVIE = "MOVIE".toByteArray()
    open var TYPE_CAMERA = "CAMERA".toByteArray()
    open var TYPE_NONE = "NONE".toByteArray()

    // +-+-+-+-+-+-+データ送受信ルール+-+-+-+-+-+-+-+-+-+
    // 一回目 : データが送られてくる通知
    // 二回目 : データの種類に関する情報
    // 三回目 : 送られてくる総データ量
    // 四回目 ～ : 実データ（総データ量を満たすまで繰り返し送信される
    // 〇回目 : データ送信完了通知（実データの量により回数が変わる）


    // データ読み込みが終了したときにまとめて返すためのリスナー
    private var listener: OnBluetoothDataResult? = null
    fun setOnBluetoothDataResultListener(l:OnBluetoothDataResult){
        listener = l
    }



    // 外で使う変数
    enum class DATA_TYPE {
        TEXT,
        IMAGE,
        MOVIE,
        CAMERA,
        NONE
    }


    //////////////////////////////////////////////////////////////////////////////////////////
    // read
    private var READ_BOOL = false
    private var READ_MAX_BYTE_SIZE = 0
    private var READ_PARAM_MAX_BYTE_SIZE = 0
    private var READ_BYTE_DATA = byteArrayOf()
    private var READ_BYTE_COUNT = 0
    private val READ_PARAM = ""
    private var READ_BYTE_TYPE = DATA_TYPE.NONE


    fun read(tmpIn: InputStream): Boolean {
        val buffer = ByteArray(ONE_BYTE)

        var bytes = -1
        Log.i(TAG, "read()")
        try {
            bytes = tmpIn.read(buffer)

        } catch (e: Exception) {
            //Log.e(TAG, "CONNECT_LOST : $e")
            return true
        }

        if (bytes > 0) {
            val next = ByteArray(bytes)
            for (i in 0 until bytes) {
                next[i] = buffer[i]
            }

            readData(next.size, next)
        }
        return false
    }

    private var miman_byte = byteArrayOf()


    //データ読み込み
    fun readData(bytes: Int, buffer: ByteArray) {
        Log.i(TAG, "readData() bytes:$bytes")
        Log.i(TAG, "readData() READ_BOOL:$READ_BOOL")
        Log.i(TAG, "readData() read_data:" + String(buffer))

        if (READ_BOOL) {
            addReadData(bytes, buffer)
            return
        }

        if (true) {// 受信したデータを一時的に格納する
            val prev = miman_byte
            miman_byte = ByteArray(prev.size + buffer.size)

            var count = 0
            for (i in prev.indices) {
                miman_byte[count] = prev[i]
                count++
            }

            for (i in buffer.indices) {
                miman_byte[count] = buffer[i]
                count++
            }

        }

        if (miman_byte.size < START_POSITION_PARAM) {
            Log.d(TAG + "_Read", "データが最低限数以下しかないので配列に格納して終わり")
            //Log.d(TAG+"_Read", "readData() : miman_byte:" + new String(miman_byte));
            return
        }


        val base_code = String(START_CODE)

        val CODE = getReadCode(miman_byte)
        val code_text = String(CODE)


        if (code_text == base_code) {
            READ_BOOL = true

            //Log.i(TAG+"_Read","ALLDATA:"+ new String(miman_byte));

            //データを取り出す
            val TYPE = getReadType(miman_byte)
            val SIZE = getReadSize(miman_byte)
            val PARAM_SIZE = getReadParamSize(miman_byte)

            READ_MAX_BYTE_SIZE = Integer.parseInt(String(SIZE))
            READ_PARAM_MAX_BYTE_SIZE = Integer.parseInt(String(PARAM_SIZE))

            //データ（パラメータと実データ）を格納する配列を作成
            READ_BYTE_DATA = ByteArray(READ_MAX_BYTE_SIZE + READ_PARAM_MAX_BYTE_SIZE)

            val DATA = getReadData(START_POSITION_PARAM, miman_byte)


            READ_BYTE_COUNT = 0

            Log.i(TAG + "_Read", "CODE        : " + String(CODE))
            Log.i(TAG + "_Read", "TYPE        : " + String(TYPE))
            Log.i(TAG + "_Read", "SIZE        : " + String(SIZE))
            Log.i(TAG + "_Read", "PARAM_SIZE  : " + String(PARAM_SIZE))
            Log.i(TAG + "_Read", "DATA        : " + String(DATA))


            val type_text = String(TYPE)
            if (type_text == String(TYPE_TEXT)) {
                READ_BYTE_TYPE = DATA_TYPE.TEXT
            } else if (type_text == String(TYPE_IMAGE)) {
                READ_BYTE_TYPE = DATA_TYPE.IMAGE
            } else if (type_text == String(TYPE_MOVIE)) {
                READ_BYTE_TYPE = DATA_TYPE.MOVIE
            } else if (type_text == String(TYPE_CAMERA)) {
                READ_BYTE_TYPE = DATA_TYPE.CAMERA
            } else {
                READ_BYTE_TYPE = DATA_TYPE.NONE
            }

            miman_byte = byteArrayOf()

            addReadData(DATA.size, DATA)

        } else {
            Log.e(TAG + "_Read", "コードがおかしかったのでスタートコードがないかチェック")

            val TYPE = getReadType(miman_byte)
            val SIZE = getReadSize(miman_byte)
            val PARAM_SIZE = getReadParamSize(miman_byte)

            Log.e(TAG + "_Read", "miman_byte : " + String(miman_byte))
            Log.e(TAG + "_Read", "CODE : " + String(CODE))
            //Log.e(TAG+"_Read", "TYPE : " + new String(TYPE));
            //Log.e(TAG+"_Read", "SIZE : " + new String(SIZE));
            //Log.e(TAG+"_Read", "KEY  : " + new String(KEY));

            val start_code_ari = checkStartCode(miman_byte)
            if (start_code_ari.size > 0) {
                Log.w(TAG + "_Read", "スタートコードがあったので、改めて処理を実行する")
                miman_byte = byteArrayOf()
                readData(start_code_ari.size, start_code_ari)
            }


        }

    }

    //実データを格納する、指定数を格納したら値をリスナーで返す。データが余ったら続きを取得しやり直し
    private fun addReadData(bytes: Int, buffer: ByteArray) {
        Log.i(TAG, "addReadData()")


        if (buffer.size == 0) {
            Log.i(TAG, "addReadData() 追加するべきデータがまだ来てないのでこのまま何もしない")
            return
        }

        val MAC_COUNT = READ_PARAM_MAX_BYTE_SIZE + READ_MAX_BYTE_SIZE

        var cnt = 0
        for (i in buffer.indices) {
            if (READ_BYTE_COUNT >= MAC_COUNT) {
                break
            }
            READ_BYTE_DATA[READ_BYTE_COUNT] = buffer[i]
            READ_BYTE_COUNT++
            cnt++

            listener?.onReadHandler(READ_BYTE_COUNT,MAC_COUNT)
        }


        //Log.i(TAG+"_Read","addReadData() cnt:"+cnt + " / size:" + buffer.length);
        Log.i(TAG + "_Read", "addReadData() READ_BYTE_COUNT:$READ_BYTE_COUNT")
        Log.i(TAG + "_Read", "addReadData() BYTE_SIZE      :$MAC_COUNT")

        if (READ_BYTE_COUNT < MAC_COUNT) {
            Log.w(TAG + "_Read", "addReadData() 想定バイト数を読み込んでないのでこれで終了")
            return
        }
        //Log.i(TAG,"addReadData() >> byte_size:" + READ_BYTE_DATA.length);


        val PARAM = getReadParam(READ_PARAM_MAX_BYTE_SIZE, READ_BYTE_DATA)
        val BYTE_DATA = getReadData(PARAM.size, READ_BYTE_DATA)


        // 想定バイト数を読み込んだので値を返す
        Log.w(TAG + "_Read", "addReadData() 想定バイト数を読み込んだので値を返す")
        Log.w(TAG + "_Read", "addReadData() READ_PARAM:" + String(PARAM))
        Log.w(TAG + "_Read", "addReadData() BYTE_DATA:" + String(BYTE_DATA))

        if (listener != null){
            listener?.onReadByte(PARAM, BYTE_DATA, READ_BYTE_TYPE)
        }

        //データの初期化
        READ_BYTE_DATA = byteArrayOf()
        READ_BYTE_TYPE = DATA_TYPE.NONE
        READ_BYTE_COUNT = 0
        READ_MAX_BYTE_SIZE = 0
        READ_PARAM_MAX_BYTE_SIZE = 0
        READ_BOOL = false


        // 想定バイト数を受信処理後、余ったバイト情報があればまとめる
        if (cnt < buffer.size) {
            //Log.w(TAG+"_Read","addReadData() 想定バイト数を受信処理後、余ったバイト情報があったので整理してから再実施");

            //bufferのあまりの開始位置
            val start_count = cnt

            val amari_count = buffer.size - cnt

            val amariByte = ByteArray(amari_count)


            //Log.w(TAG+"_Read","addReadData() cnt:" + cnt);
            //Log.w(TAG+"_Read","addReadData() amari_count:" + amari_count);
            //Log.w(TAG+"_Read","addReadData() buffer size:" + buffer.length);

            for (i in 0 until amari_count) {
                amariByte[i] = buffer[i + start_count]
            }


            //Log.w(TAG+"_Read","addReadData() amariByte[]:" + new String(amariByte));

            //余ったバイトデータを受信情報としてとして返しておく
            val next_bytes = amariByte.size
            readData(next_bytes, amariByte)
        }
    }

    //指定場所から残りのデータを全て入れ直す
    private fun amariParse(cnt: Int, buffer: ByteArray): ByteArray {
        //残りのデータをまとめる
        val next = ByteArray(buffer.size - cnt)

        val n_cnt = 0
        for (i in cnt until buffer.size) {
            next[n_cnt] = buffer[i]
        }

        return next
    }


    private fun checkStartCode(buffer: ByteArray): ByteArray {

        var start_code_enable = false
        var next_byte = byteArrayOf()

        val start = -1
        for (i in buffer.indices) {
            if (buffer[i] == START_CODE[0] && buffer.size - i >= START_CODE.size) {
                start_code_enable = true

                for (j in 0 until START_CODE.size) {
                    if (buffer[i + j] == START_CODE[j]) {

                    } else {
                        start_code_enable = false
                        break
                    }
                }


                if (start_code_enable) {
                    //START_CODEが見つかったので、分割する

                    next_byte = ByteArray(buffer.size - i)

                    for (j in next_byte.indices) {
                        next_byte[j] = buffer[i + j]
                    }
                    break
                }
            }
        }

        return next_byte
    }


    private fun getReadCode(buffer: ByteArray): ByteArray {
        return getBaseReadParam(buffer, CODE_COUNT, START_POSITION_CODE)
    }


    /**
     * 読み込んだBufferからデータのタイプが格納された部分を取り出し整理する
     * @param buffer
     * @return
     */
    private fun getReadType(buffer: ByteArray): ByteArray {
        return getBaseReadParam(buffer, TYPE_COUNT, START_POSITION_TYPE)
    }

    /**
     * 読み込んだBufferからデータのサイズが格納された部分を取り出し整理する
     * @param buffer
     * @return
     */
    private fun getReadSize(buffer: ByteArray): ByteArray {
        return getBaseReadParam(buffer, SIZE_COUNT, START_POSITION_SIZE)
    }

    /**
     * 読み込んだBufferからデータのサイズが格納された部分を取り出し整理する
     * @param buffer
     * @return
     */
    private fun getReadParamSize(buffer: ByteArray): ByteArray {
        return getBaseReadParam(buffer, PARAM_SIZE_COUNT, START_POSITION_PARAM_SIZE)
    }

    /**
     * DataとParamがくっついたバッファーbyte配列からParamのデータを取得する
     */
    private fun getReadParam(count: Int, buffer: ByteArray): ByteArray {
        return getBaseReadParam(buffer, count, 0)
    }

    private fun getBaseReadParam(buffer: ByteArray, count: Int, start_position: Int): ByteArray {
        val n:Byte = 0
        var cnt = 0
        val prevtype = ByteArray(count)

        for (i in 0 until count) {
            if (buffer[i + start_position] == n) {

            } else {
                prevtype[cnt] = buffer[i + start_position]
                cnt++
            }
        }

        val type = ByteArray(cnt)
        for (i in 0 until cnt) {
            type[i] = prevtype[i]
        }

        return type
    }


    /**
     * 読み込んだBufferからデータのキー情報が格納された部分を取り出し整理する
     */
    private fun getReadData(start_position_count: Int, buffer: ByteArray): ByteArray {

        val count = buffer.size - start_position_count

        val data = ByteArray(count)

        for (i in data.indices) {
            data[i] = buffer[i + start_position_count]
        }

        return data
    }

    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 送信処理
     * @param type
     * @param bytes
     * @throws IOException
     */

    fun setHeader(code: ByteArray, type: DATA_TYPE, bytes: Int, param_size: Int): ByteArray {

        // 開始コード
        val SEND_CODE = getWriteCode(code)

        // タイプ情報を10バイトの配列にまとめる
        val SEND_TYPE = getWriteType(type)

        // データサイズを10バイトの配列にまとめる
        val SNED_SIZE = getWriteSize(bytes)

        // 送信するデータと紐づけするためのキーを配列にまとめる
        val SEND_PARAM_SIZE = getWriteParamSize(param_size)

        val param_size = SEND_CODE.size + SEND_TYPE.size + SNED_SIZE.size + SEND_PARAM_SIZE.size


        Log.d(TAG, "setHeader() : SEND_CODE : " + String(SEND_CODE))
        Log.d(TAG, "setHeader() : SEND_TYPE : " + String(SEND_TYPE))
        Log.d(TAG, "setHeader() : SNED_SIZE : " + String(SNED_SIZE))
        Log.d(TAG, "setHeader() : SEND_PARAM_SIZE  : " + String(SEND_PARAM_SIZE))

        //実際に送信するデータを格納するバイト破裂
        val send_buffer = ByteArray(param_size)

        var count = 0

        //開始コードを格納
        for (i in SEND_CODE.indices) {
            send_buffer[count] = SEND_CODE[i]
            count++
        }

        //タイプ情報を格納
        for (i in SEND_TYPE.indices) {
            send_buffer[count] = SEND_TYPE[i]
            count++
        }

        //サイズ情報を格納
        for (i in SNED_SIZE.indices) {
            send_buffer[count] = SNED_SIZE[i]
            count++
        }

        //キー情報を格納
        for (i in SEND_PARAM_SIZE.indices) {
            send_buffer[count] = SEND_PARAM_SIZE[i]
            count++
        }
        return send_buffer
    }

    fun setParamAndData( param_buffer: ByteArray, writeBuffer: ByteArray):ByteArray {
        val send_buffer = ByteArray(param_buffer.size + writeBuffer.size)

        var count = 0
        for(i in 0 until param_buffer.size){
            send_buffer[count] = param_buffer[i]
            count++
        }
        for(i in 0 until writeBuffer.size){
            send_buffer[count] = writeBuffer[i]
            count++
        }

        return send_buffer
    }


    @SuppressLint("LongLogTag")
    @Throws(IOException::class)
    fun write(outStream: OutputStream, param_buffer: ByteArray, writeBuffer: ByteArray) {

        try{
            val all_size = writeBuffer.size

            Log.i(TAG+"_Write","write() ---------------------------------------------------------")
            Log.i(TAG+"_Write","param buffer:" + String(param_buffer))
            Log.i(TAG+"_Write","write buffer:" + String(writeBuffer))

            ////////////////////////////////////////
            //パラメータを送信する
            outStream.write(param_buffer)


            ////////////////////////////////////////
            // データ送信

            if (all_size < ONE_BYTE) {
                //サイズが想定サイズなので一括で送信

                //Log.d(TAG, "write() : SEND_DATA  : " + String(writeBuffer))

                outStream.write(writeBuffer)

            } else {
                //サイズが大きいので分割して送信

                val write_wari = all_size / ONE_BYTE
                val write_amari = all_size % ONE_BYTE

                var loop_size = write_wari
                if(write_amari != 0){
                    loop_size += 1
                }

                var byte_count = 0
                for (i in 0 until loop_size) {
                    //分割のサイズ分のバイト配列を設定
                    var send_byte = ByteArray(ONE_BYTE)

                    if (i == (i - 1)) {
                        // 最後なので残りを入れる分の配列を作る
                        send_byte = ByteArray(write_amari)
                    }

                    for (j in send_byte.indices) {
                        if (byte_count < writeBuffer.size) {
                            send_byte[j] = writeBuffer[byte_count]
                            byte_count++
                        }
                    }
                    outStream.write(send_byte)
                }
            }

            outStream.flush()

        }catch(e:Exception){

        }

        return
    }





    /**
     * 送信時にタイプ情報を10バイトの配列に格納する処理
     * @return
     */
    fun getWriteCode(code: ByteArray): ByteArray {

        val bytes = ByteArray(CODE_COUNT)
        var cnt = 0
        for (i in 0 until bytes.size) {
            bytes[i] = code[cnt]
            cnt++
        }
        return bytes
    }

    /**
     * 送信時にタイプ情報を10バイトの配列に格納する処理
     * @param type
     * @return
     */
    fun getWriteType(type: DATA_TYPE): ByteArray {
        var select = byteArrayOf()

        if (type == DATA_TYPE.TEXT) {
            select = TYPE_TEXT
        } else if (type == DATA_TYPE.IMAGE) {
            select = TYPE_IMAGE
        } else if (type == DATA_TYPE.MOVIE) {
            select = TYPE_MOVIE
        } else if (type == DATA_TYPE.CAMERA) {
            select = TYPE_CAMERA
        } else {
            select = TYPE_NONE
        }


        val bytes = ByteArray(TYPE_COUNT)
        for (i in 0 until select.size) {
            bytes[i] = select[i]
        }
        return bytes
    }

    /**
     * 送信時にデータのサイズ情報を10バイトの配列に格納する処理
     * @param count
     * @return
     */
    fun getWriteSize(count: Int): ByteArray {

        val count_s = "" + count
        val cnt_buf = count_s.toByteArray()

        val bytes = ByteArray(SIZE_COUNT)
        for (i in 0 until cnt_buf.size) {
            bytes[i] = cnt_buf[i]
        }
        return bytes
    }
    fun getWriteParamSize(count: Int): ByteArray {

        val count_s = "" + count
        val cnt_buf = count_s.toByteArray()

        val bytes = ByteArray(PARAM_SIZE_COUNT)
        for (i in 0 until cnt_buf.size) {
            bytes[i] = cnt_buf[i]
        }
        return bytes
    }

    /**
     * 送信時にデータのキー情報を20バイトの配列に格納する処理
     */
    fun getWriteParam(param: String): ByteArray {
        val select = param.toByteArray()
        return select
    }


}
