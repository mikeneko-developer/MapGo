package net.mikemobile.navi.core.bluetooth;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import net.mikemobile.navi.bluetooth.BluetoothReadWrite;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class BluetoothData {
    private static final String TAG = "BluetoothData";

    // BluetoothReadWriteに最終的に投げるデータ
    public byte[] buffer;
    public byte[] code = BluetoothReadWrite.START_CODE;
    public BluetoothReadWrite.DATA_TYPE type = BluetoothReadWrite.DATA_TYPE.NONE;
    public byte[] param;

    public int getParamSize(){
        if(param == null)return 0;
        return param.length;
    }

    public void setType(BluetoothReadWrite.DATA_TYPE type){
        this.type = type;
    }

    public void setData(byte[] bytes){
        buffer = bytes;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static final int ID_COMMAND = 1;
    public static final int ID_SAY_TEXT = 2;
    public static final int ID_IMAGE_LIST_ITEM = 3;//現在ある画像のリストを取得する
    public static final int ID_IMAGE_THUMBNAIL_SEND_BYTE_DATA = 6;
    public static final int ID_IMAGE_SEND_BYTE_DATA = 4;//画像のパスデータの取得およびByteデータの返却
    public static final int ID_MOVIE = 5;


    public int id = -1;
    public int number = 0;
    public String command = "";
    public String data = "";

    public String text = "";
    public String motion_id = "";
    public String emotion_id = "";

    public ArrayList<HashMap<String,Object>> array = new ArrayList<HashMap<String,Object>>();
    public HashMap<String,Object> map = new HashMap<String,Object>();



    public void setParamByteData(byte[] bytes){
        String data_text = new String(bytes);
        try {
            JSONObject json = new JSONObject(data_text);

            id = json.getInt("id");
            number = json.getInt("number");
            command = json.getString("command");
            data = json.getString("data");
            text = json.getString("text");

            Log.i(TAG,"setParamByteData id : " + id);
            Log.i(TAG,"setParamByteData number : " + number);
            Log.i(TAG,"setParamByteData command : " + command);
            Log.i(TAG,"setParamByteData data : " + data);
            Log.i(TAG,"setParamByteData text : " + text);


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //読み込んだバイト配列をパラメータを参照の上格納する
    public void setByteData(byte[] bytes){

        if(type != BluetoothReadWrite.DATA_TYPE.TEXT){
            buffer = bytes;
        }

        Log.i(TAG,"read data : " + new String(bytes));

        String data_text = new String(bytes);
        try {
            JSONObject json = new JSONObject(data_text);


            if(id == ID_COMMAND){
                //text = json.getString("text");

            }else if(id == ID_SAY_TEXT){
                //text = json.getString("text");
                motion_id = json.getString("motion_id");
                emotion_id = json.getString("emotion_id");

                Log.i(TAG,"read motion_id : " + motion_id);
                Log.i(TAG,"read emotion_id : " + emotion_id);

            }else if(id == ID_IMAGE_LIST_ITEM){
                //text = json.getString("text");

                String array_text = json.getString("array");
                JSONArray objArray = new JSONArray(array_text);

                ArrayList<HashMap<String,Object>> arrayItem = new ArrayList<HashMap<String,Object>>();
                for(int i=0;i<objArray.length();i++){
                    JSONObject objItem = objArray.getJSONObject(i);

                    HashMap<String,Object> item = new HashMap<String,Object>();
                    item.put("name",(String)objItem.get("name"));
                    item.put("path",(String)objItem.get("path"));

                    arrayItem.add(item);
                }

                array = arrayItem;

                /**
                 String map_text = json.getString("map");

                 JSONObject objItem = new JSONObject(map_text);
                 HashMap<String,Object> item = new HashMap<String,Object>();
                 item.put("name",(String)objItem.get("name"));
                 item.put("path",(String)objItem.get("path"));

                 map = item;
                 */
            }else {

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //送信するための実データを生成して返す
    public void getByteData(){

        try {
            JSONObject json = new JSONObject();
            json.put("id",id);
            json.put("command",command);
            json.put("number",number);
            json.put("data",data);
            json.put("text",text);

            String json_text = json.toString();

            param = json_text.getBytes();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(type != BluetoothReadWrite.DATA_TYPE.TEXT){
            if(buffer == null)buffer = new byte[]{};
            return;
        }

        JSONObject json = new JSONObject();
        try {
            json.put("motion_id",motion_id);
            json.put("emotion_id",emotion_id);

            JSONObject jMap = new JSONObject(map);
            json.put("map",jMap.toString());

            JSONArray jArray = new JSONArray(array);
            json.put("array",jArray.toString());

            String json_text = json.toString();

            buffer = json_text.getBytes();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /////////////////////////////////////////////////////////////////////////////////////

    public void setArray(int id,ArrayList<HashMap<String,Object>> item){
        this.id = id;
        this.array = item;
    }

    public void setHash(int id,HashMap<String,Object> item){
        this.id = id;
        this.map = item;
    }

    /////////////////////////////////////////////////////////////////////////////////////


    /////////////////////////////////////////////////////////////////////////////////////
    public Bitmap setImageData(int id,HashMap<String,Object> item){

        this.id = id;
        String path = (String) item.get("path");
        Bitmap bitmap = null;
        try {

            // 情報のみ読み込む
            bitmap = createSize(path,500,500);
            buffer = getBitmapAsByteArray(bitmap);
            //bitmap.recycle();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"error : " + e.toString());
        }

        return bitmap;
    }

    public Bitmap setImageThumbnailData(int id,HashMap<String,Object> item){


        this.id = id;
        String path = (String) item.get("path");
        Bitmap bitmap = null;
        try {

            // 情報のみ読み込む
            bitmap = createSize(path,100,100);
            buffer = getBitmapAsByteArray(bitmap);
            //bitmap.recycle();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,"error : " + e.toString());
        }

        return bitmap;
    }


    private byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //PNG, クオリティー100としてbyte配列にデータを格納
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private Bitmap createSize(String path,int width,int height){

        BitmapFactory.Options option = new BitmapFactory.Options();

        // 情報のみ読み込む
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, option);

        if (option.outWidth < width || option.outHeight < height) {
            // 縦、横のどちらかが指定値より小さい場合は普通にBitmap生成
            return BitmapFactory.decodeFile(path);
        }

        float scaleWidth = ((float) width) / option.outWidth;
        float scaleHeight = ((float) height) / option.outHeight;

        int newSize = 0;
        int oldSize = 0;
        if (scaleWidth > scaleHeight) {
            newSize = width;
            oldSize = option.outWidth;
        } else {
            newSize = height;
            oldSize = option.outHeight;
        }

        // option.inSampleSizeに設定する値を求める
        // option.inSampleSizeは2の乗数のみ設定可能
        int sampleSize = 1;
        int tmpSize = oldSize;
        while (tmpSize > newSize) {
            sampleSize = sampleSize * 2;
            tmpSize = oldSize / sampleSize;
        }
        if (sampleSize != 1) {
            sampleSize = sampleSize / 2;
        }

        option.inJustDecodeBounds = false;
        option.inSampleSize = sampleSize;

        return BitmapFactory.decodeFile(path, option);

    }
}
