package net.mikemobile.navi.core.gps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by systena on 16/09/06.
 */
@SuppressLint("NewApi")
public class MyAccess extends AsyncTask<Integer, Integer, MyAccess.MyWebResponse> {
    /** ============================================================================================ **/
    // 定数
    /** ============================================================================================ **/
    public static final String TAG = MyAccess.class.getName();

    public static final int POST = 0;
    public static final int GET = 1;

    public static final int ACCESS_OK = HttpURLConnection.HTTP_OK;

    private String    webEncode    = "utf-8";
    /** ============================================================================================ **/
    // 変数
    /** ============================================================================================ **/
    private Context context;

    private String url;
    private int type = GET;

    private String url_param = null;
    private JSONObject body_param;

    private Exception error = null;

    private CookieManager cookieManager;
    private boolean encode = false;


    //ログを見た時、共通の処理を見やすくするための値（重複したばあいどれがどれかわからないため）
    private String random = "";

    /** ============================================================================================ **/
    // コンストラクタ
    /** ============================================================================================ **/
    public MyAccess(Context context){
        super();

        int ran = (int)(Math.random() * 1000) + 1;
        random = "" + ran;
        if(ran < 10){
            random = "000" + ran;
        }else if(ran < 100){
            random = "00" + ran;
        }else if(ran < 1000){
            random = "0" + ran;
        }
        random = random + "";

        Log.w(TAG + random,"MyNetwork");
        //初期化
        this.context = context;
        url_param = "";
        type = GET;
        error = null;

        body_param = new JSONObject();
    }

    /** ============================================================================================ **/
    // 継承メソッド
    /** ============================================================================================ **/

    // doInBackground前に実行する処理
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.w(TAG + random,"onPreExecute");

        //Cookieの初期化とログイン情報を入れる
        cookieManager = MyAccess.setCookie(context,random);


    }

    // バックグラウンドで実行する処理を記述するメソッド
    @Override
    protected MyWebResponse doInBackground(Integer... integers) {
        Log.w(TAG + random,"doInBackground");

        if(url == null || url.equals("")){
            Log.e(TAG + random,"url is null");
            return null;
        }

        MyWebResponse result = null;



        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();

        if (nInfo == null || !nInfo.isConnected()) {
            Log.w(TAG, "ネットに繋がっていないのできります");

            result = new MyWebResponse();
            result.setStatus(-999999);
            result.setError("ネットにつながっていません");

            return result;
        }

        /** -----------------------認証開始------------------------- **/
        Log.w(TAG + random,"通信処理を実行");
        switch(type){
            case POST:result = executePost(url);
                break;
            case GET:result = executeGet(url);

                break;
        }


        return result;
    }

    // 進捗状況をUIスレッドで表示するためのメソッド
    @Override
    protected void onProgressUpdate(Integer... progress) {
        //progress[0]
        Log.w(TAG + random,"onProgressUpdate");

    }

    // バックグラウンド処理が完了しUIスレッドに反映するためのメソッド
    @Override
    protected void onPostExecute(MyWebResponse obj) {
        super.onPostExecute(obj);
        Log.w(TAG + random,"onPostExecute");

        if(listener != null){
            listener.AccessResult(obj,error);
        }
    }

    /** ============================================================================================ **/
    // メソッド
    /** ============================================================================================ **/
    //接続
    public void access(){
        Log.w(TAG + random,"access");

        this.execute(1);
    }
    //GET POST 選択
    public void setAccsessType(int type){
        Log.w(TAG + random,"setAccsessType : " + type + "   POST="+POST+" GET="+GET);
        this.type = type;
    }
    //URL指定
    public void setURL(String url_text){
        Log.w(TAG + random,"setURL : " + url_text);
        url = url_text;
    }

    //URLパラメータを指定
    public void setParam(String key,int value){
        setParam(key,String.valueOf(value));
    }
    public void setParam(String key,String value){
        Log.w(TAG + random,"setParam : " + key + " / " + value);
        if(url_param.equals("")){
            url_param = "?" + key + "=" + value;
        }else {
            url_param += "&" + key + "=" + value;
        }
    }

    public void setData(String key,int value){
        Log.w(TAG + random,"setData : " + key + " / " + value);
        try {
            body_param.put(key,value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void setData(String key,String value){
        Log.w(TAG + random,"setData : " + key + " / " + value);
        try {
            body_param.put(key,value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void setData(String key,Long value){
        Log.w(TAG + random,"setData : " + key + " / " + value);
        try {
            body_param.put(key,value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setData(String key,JSONArray value){
        Log.w(TAG + random,"setData : " + key + " / " + value);
        try {
            body_param.put(key,value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setData(String key,JSONObject value){
        Log.w(TAG + random,"setData : " + key + " / " + value);
        try {
            body_param.put(key,value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void setData(JSONObject obj){
        body_param = obj;
    }

    public String getEncode(String text){
        try {
            return URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return text;
    }


    /** ========================================================================= **/
    //通信
    private MyWebResponse executePost(String url_text){
        Log.d(TAG + random,"===== HTTP POST Start =====");

        MyWebResponse res = null;


        try {
            URL url = new URL(url_text);

            //String cookie = cookieManager.getCookie(Global.DOMAIN);
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Accept-Language", "ja");
                //connection.addRequestProperty("Cookie", cookie + ";");

                //Log.d(TAG + random,"cookie : " + cookie);

                PrintStream ps = new PrintStream(connection.getOutputStream());
                Log.d(TAG + random,"param : " + body_param.toString());
                ps.print("" + body_param.toString());

                ps.close();

                //MyWebResponse res = getResponse(connection,webEncode);
                //Log.d(TAG + random,"body : " + res.getBody());

                res = getResponse(connection,webEncode);

                Log.d(TAG + random,"connect result code : " + res.status);
                Log.d(TAG + random,"connect result body : " + res.getBody());

                setCookie(connection);


            } finally {
                error = null;
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } catch (IOException e) {
            error = e;
            Log.e(TAG + random,"executeGET IOException : " + e.toString());
            res = new MyWebResponse();
            res.setStatus(-999999);
            res.setError("通信に失敗しました");

        }


        Log.d(TAG + random,"===== HTTP POST End =====");
        Log.d(TAG + random,"result : " + res.getBody());

        return res;
    }
    private MyWebResponse executeGet(String url_text){
        Log.d(TAG + random,"===== HTTP GET Start =====");

        MyWebResponse res = null;
        try {
            if(url_param == null){
                url_param = "";
            }

            Log.d(TAG + random,"param : " + url_param);

            URL url = new URL(url_text + url_param);

            //String cookie = cookieManager.getCookie(Global.DOMAIN);
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept-Language", "ja");
                //connection.addRequestProperty("Cookie", cookie + ";");

                //Log.d(TAG + random,"cookie : " + cookie);

                res = getResponse(connection,webEncode);

                Log.d(TAG + random,"connect result code : " + res.status);
                Log.d(TAG + random,"connect result body : " + res.getBody());

                setCookie(connection);


            } finally {
                error = null;
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } catch (IOException e) {
            error = e;
            Log.e(TAG + random,"executeGET IOException : " + e.toString());
            res = new MyWebResponse();
            res.setStatus(-999999);
            res.setError("通信に失敗しました");

        }
        Log.d(TAG + random,"===== HTTP GET End =====");
        Log.d(TAG + random,"result : " + res.getBody());

        return res;
    }


    static final String COOKIES_HEADER = "Set-Cookie";

    public void setCookie(HttpURLConnection httpClient){
        Log.w(TAG + random,"setCookie ===============");

        Map<String, List<String>> cookieStr = httpClient.getHeaderFields();
        Iterator<String> headerIt = cookieStr.keySet().iterator();
        while (headerIt.hasNext()) {
            String key = headerIt.next();
            List<String> valList = cookieStr.get(key);
            if (key != null) {
                StringBuilder sb = new StringBuilder();
                for (String val : valList) {
                    if (sb.length() > 0)
                        sb.append("\n");
                    sb.append(val);
                }


                //Log.w(TAG + random,"key : " + key + " value : " + sb.toString());

            }
        }



    }

    public void setOnMyNetworkListener(MyNetworkListener l){
        listener = l;
    }


    //Cookieデータを設定
    public static CookieManager setCookie(Context context,String random){

        CookieManager cookieManager = CookieManager.getInstance();

        /**
        String accountName = LocalSave.getUserAccount(context);
        String password = LocalSave.getUserPassword(context);
        String token = LocalSave.getUserToken(context);

        Log.w(TAG + random,"accountName : " + accountName);
        Log.w(TAG + random,"password : " + password);
        Log.w(TAG + random,"token : " + token);

        if(accountName != null && password != null){
            cookieManager.setCookie(Global.DOMAIN,"accountName" + "=" + accountName + ";");
            cookieManager.setCookie(Global.DOMAIN,"password" + "=" + password + ";");
            cookieManager.setCookie(Global.DOMAIN,"token" + "=" + token + ";");


            //don't delete tokenがうまく取得できない理由に最後の値に「;」がつかないため、暫定処置
            cookieManager.setCookie(Global.DOMAIN,"dummy" + "=none;");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Log.i(TAG + random,"クッキーに保存");
            cookieManager.flush();
        }else {
            //Log.i(TAG + random,"クッキーに保存しません");
        }
        Log.i(TAG + random,"クッキーに保存");
*/
        return cookieManager;
    }
    //Cookieデータを削除
    public static void removeCookie(Context context) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        cookieManager.setAcceptCookie(true);
    }


    /*
     * レスポンスデータを取得する。<br />
     * @param http http接続オブジェクト
     * @param webEncode エンコーディング
     */
    private MyWebResponse getResponse(HttpURLConnection http, String webEncode) throws IOException {

        Log.i(TAG + random,"getResponse");

        MyWebResponse response = new MyWebResponse();
        // ステータスコードの取得
        response.setStatus(http.getResponseCode());
        Log.i(TAG + random,"code : " + http.getResponseCode());


        // ヘッダの取得
        Log.i(TAG + random,"ヘッダの取得");
        LinkedHashMap<String, String> resHeader = new LinkedHashMap<String, String>();
        Map<String, List<String>> header = http.getHeaderFields();
        Iterator<String> headerIt = header.keySet().iterator();

        while (headerIt.hasNext()) {
            String key = headerIt.next();
            List<String> valList = header.get(key);
            if (key != null) {
                StringBuilder sb = new StringBuilder();
                for (String val : valList) {
                    if (sb.length() > 0)
                        sb.append("\n");
                    sb.append(val);
                }
                resHeader.put(key, sb.toString());
            }
        }
        response.setHeader(resHeader);

        // ボディ(コンテンツ)の取得
        Log.i(TAG + random,"ボディ(コンテンツ)の取得");
        try{
            InputStream is = http.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, webEncode));
            StringBuilder sbBody = new StringBuilder();
            String s;
            while ((s = reader.readLine()) != null) {
                sbBody.append(s);
                sbBody.append("\n");
            }
            response.setBody(sbBody.toString());
        }catch(java.io.FileNotFoundException e){
            Log.e(TAG + random,"" + e.toString());
        }


        return response;
    }
    /** ============================================================================================ **/
    // 内部クラス（interfaceなど）
    /** ============================================================================================ **/
    private MyNetworkListener listener = new MyNetworkListener() {
        @Override
        public void AccessResult(MyWebResponse result, Exception e) {
        }
    };



    public interface MyNetworkListener {
        public abstract void AccessResult(MyWebResponse result, Exception e);
    }


    public class MyWebResponse {
        private int                             status      = 0;
        private LinkedHashMap<String, String>   header      = new LinkedHashMap<String, String>();
        private String                          body        = "";
        private List<Map>                       cookies     = null;
        private String                          error       = "";
        private String                          error_code  = "";

        public int getStatus() {
            return status;
        }
        public void setStatus(int status) {
            this.status = status;
        }

        public LinkedHashMap<String, String> getHeader() {
            return header;
        }
        public void setHeader(LinkedHashMap<String, String> header) {
            this.header = header;
        }

        public String getBody() {
            return body;
        }
        public void setBody(String body) {
            this.body = body;
        }

        public String getError(){
            return error;
        }
        public void setError(String text){
            this.error = text;
        }

        public String getErrorCode(){
            return error_code;
        }
        public void setErrorCode(String code){
            this.error_code = code;
        }


        public List<Map> getCookies() {
            if (cookies != null) {
                return cookies;
            }
            cookies = new ArrayList();;
            Map headers = getHeader();
            Iterator it = headers.keySet().iterator();
            while (it.hasNext()){
                String key = (String)it.next();
                if ("Set-Cookie".equals(key)) {
                    String val = headers.get(key).toString();
                    Matcher m = Pattern.compile("^([a-zA-Z_]+)=([^;]+); path=(.+)$").matcher(val);
                    if (m.find()){
                        String cookieKey  = m.group(1);
                        String cookieVal  = m.group(2);
                        String cookiePath = m.group(3);
                        Map cookie = new HashMap();
                        cookie.put("key", cookieKey);
                        cookie.put("val", cookieVal);
                        cookie.put("path", cookiePath);
                        cookies.add(cookie);
                    }
                }
            }
            return cookies;
        }

        public String getCookie(String key) {
            List<Map> cookies = getCookies();
            for (Map cookie : cookies) {
                String cookieKey = (String)cookie.get("key");
                if (cookieKey.equals(key)) {
                    return (String)cookie.get("val");
                }
            }
            return "";
        }
    }

}
