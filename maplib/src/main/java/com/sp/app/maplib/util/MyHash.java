package com.sp.app.maplib.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MyHash {
	private static final boolean log = true;
	private static final String TAG = "MyHash";
	
	private static boolean check_log = true;
	//Hashの中に何があるのかチェックする
	public static void checkData(HashMap<String,Object> data) {
		String check_tag = TAG;
		checkData(check_tag,data);
	}
	
	//Hashの中に何があるのかチェックする
	public static void checkData(String tag_key, HashMap<String,Object> data) {
		Set<String> keys = data.keySet();
		Iterator<String> iterator = keys.iterator();
		
		String check_tag = TAG;
		if(tag_key != null && !tag_key.equals("")){
			check_tag = tag_key;
		}
		
		Log.d(check_tag, "================================================");
		for (int i = 0; i < data.size(); i++) {
			String key = iterator.next();

			Log.d(check_tag,"========================");
			Log.d(check_tag, "key:" + key);
			Log.d(check_tag, "data:" + data.get(key));
		}
		Log.d(check_tag, "================================================");
		
	}
	
	/** ================================================================== **/
	//Objectデータで値を取り出す
	/** ================================================================== **/
	public static Object getObject(HashMap<String,Object> data, String key) {
		try {
			Object text = data.get(key);
			
			return text;
		} catch (Exception e) {
			if (log) Log.e(TAG,"Exception Object変換失敗==========");
			if (log) Log.e(TAG, e.toString());
			
			return null;
		}
	}

	/** ================================================================== **/
	//値が「0000-00-00 00:00:00」というフォーマットの場合の取得
	/** ================================================================== **/
	public static long getPHPDateTimeToLong(HashMap<String,Object> data, String key) {
		try {
			String text = (String) data.get(key);
			
			if (text == null || text.equals("") 
					|| text.equals("null")|| text.equals("Null") || text.equals("NULL")
					|| text.equals("none")) {
				
				return getLong(data,key);
			}
			
			long date_time = 0;
			
			if(text.equals("0000-00-00 00:00:00")){
				return date_time;
			}
			
			
			String[]text1 = text.split(" ");
			
			String[]date = text1[0].split("-");
			String[]time = text1[1].split(":");

			int year = Integer.parseInt(date[0]);
			int month = Integer.parseInt(date[1]);
			int day = Integer.parseInt(date[2]);

			int hour = Integer.parseInt(time[0]);
			int minute = Integer.parseInt(time[1]);
			int second = Integer.parseInt(time[2]);
			
			date_time = MyDate.getTimeMillis(year, month, day, hour, minute, second);
			
			return date_time;
		} catch (Exception e) {
			return getLong(data,key);
		}
	}
	public static long getPHPDateToLong(HashMap<String,Object> data, String key) {
		try {
			String text = (String) data.get(key);
			
			if (text == null || text.equals("") 
					|| text.equals("null")|| text.equals("Null") || text.equals("NULL")
					|| text.equals("none")) {
				
				return getLong(data,key);
			}
			
			long date_time = 0;

			if(text.equals("0000-00-00")){
				return date_time;
			}
			
			String[]date = text.split("-");

			int year = Integer.parseInt(date[0]);
			int month = Integer.parseInt(date[1]);
			int day = Integer.parseInt(date[2]);

			int hour = 0;
			int minute = 0;
			int second = 0;
			
			date_time = MyDate.getTimeMillis(year, month, day, hour, minute, second);
			
			return date_time;
		} catch (Exception e) {
			return getLong(data,key);
		}
	}

	public static long getPHPTimeStringToLong(HashMap<String,Object> data, String key){
		try {
			String text = (String) data.get(key);
			
			if (text == null || text.equals("") 
					|| text.equals("null")|| text.equals("Null") || text.equals("NULL")
					|| text.equals("none")) {
				
				return getLong(data,key);
			}
			
			long date_time = 0;
			
			String[]time = text.split(":");
			

			int year = 1700;
			int month = 1;
			int day = 1;

			int hour = Integer.parseInt(time[0]);
			int minute = Integer.parseInt(time[1]);
			int second = Integer.parseInt(time[2]);
			
			date_time = MyDate.getTimeMillis(year, month, day, hour, minute, second);
			
			return date_time;
		} catch (Exception e) {
			return getLong(data,key);
		}
	}
	/** ================================================================== **/
	//値がString型の場合取り出す処理
	/** ================================================================== **/
	public static String getString(HashMap<String,Object> data, String key) {
		try {
			String text = (String) data.get(key);
			
			if (text == null || text.equals("") 
					|| text.equals("null")|| text.equals("Null") || text.equals("NULL")
					|| text.equals("none")) {
				return null;
			}
			return text;
		} catch (Exception e) {
			if (log) Log.e(TAG,"Exception 文字から数字変換失敗==========");
			if (log) Log.e(TAG, e.toString());
			
			return null;
		}
	}

	/** ================================================================== **/
	//値がInteger型の場合取り出す処理
	/** ================================================================== **/
	public static int getInteger(HashMap<String,Object> data, String key) {
		if (log) Log.w(TAG,"getInteger ==================================================");
		if (log) Log.w(TAG,"key:"+ key);

		try {
			Object obj = data.get(key);

			if(obj instanceof Long){
				if (log) Log.w(TAG,"Long 型");
				return Integer.parseInt(String.valueOf((Long)obj));
			}else if(obj instanceof Integer){
				if (log) Log.w(TAG,"Integer 型");
				return (Integer)obj;
			}else if(obj instanceof Double){
				if (log) Log.w(TAG,"Double 型");
				double d = (((Double)obj));
				return (int)d;
			}else if(obj instanceof Short){
				if (log) Log.w(TAG,"Short 型");
				short d = (((Short)obj));
				return (int)d;
			}else if(obj instanceof String){
				if (log) Log.w(TAG,"String 型");
				String text = (String) obj;
				if (text == null || text.equals("") || text.equals("null")
						|| text.equals("Null") || text.equals("NULL")
						|| text.equals("none") || text.equals("")) {
					text = "-1";
				}
				int number = Integer.parseInt(text);

				return number;
			}else if(obj instanceof BigDecimal){
				if (log) Log.w(TAG,"BigDecimal 型");

				BigDecimal bd = (BigDecimal)obj;
				int number = bd.intValue();

				return number;
			}

			return -1;
		} catch (Exception e) {
			if (log) Log.e(TAG, e.toString());

			BigDecimal db = getBigDecimal(data, key);
			return db.intValue();
		}

	}

	/** ================================================================== **/
	//値がInteger型の場合取り出す処理
	/** ================================================================== **/
	public static Double getDouble(HashMap<String,Object> data, String key) {
		if (log) Log.w(TAG,"getDouble ==================================================");
		if (log) Log.w(TAG,"key:"+ key);

		try {
			Object obj = data.get(key);

			if(obj instanceof Double){
				if (log) Log.w(TAG,"Double 型");
				double d = (((Double)obj));
				return d;
			}else if(obj instanceof BigDecimal){
				if (log) Log.w(TAG,"BigDecimal 型");

				BigDecimal bd = (BigDecimal)obj;
				return bd.doubleValue();
			}

			return -1d;
		} catch (Exception e) {
			if (log) Log.e(TAG, e.toString());


			BigDecimal db = getBigDecimal(data, key);
			return db.doubleValue();
		}

	}

	/** ================================================================== **/
	//値がLong型の場合取り出す処理
	/** ================================================================== **/
	public static long getLong(HashMap<String,Object> data, String key) {
		boolean log  = true;

		if (log) Log.w(TAG,"getLong ==================================================");
		if (log) Log.w(TAG,"key:"+ key);
		
		try {
			Object obj = data.get(key);
			
			if(obj instanceof Long){
				if (log) Log.w(TAG,"Long 型");
				return (Long)obj;
			}else if(obj instanceof Integer){
				if (log) Log.w(TAG,"Integer 型");
				return (long)((Integer)obj);
			}else if(obj instanceof Double){
				if (log) Log.w(TAG,"Double 型");
				double d = (((Double)obj));
				return (int)d;
			}else if(obj instanceof Short){
				if (log) Log.w(TAG,"Short 型");
				short d = (((Short)obj));
				return (int)d;
			}else if(obj instanceof String){
				if (log) Log.w(TAG,"String 型");
				String text = (String) obj;
				if (text == null || text.equals("") || text.equals("null")
						|| text.equals("Null") || text.equals("NULL")
						|| text.equals("none") || text.equals("")) {
					text = "-1";
				}
				long number = Long.parseLong(text);
				
				return number;
			}else if(obj instanceof BigDecimal){
				if (log) Log.w(TAG,"BigDecimal 型");
				
				BigDecimal bd = (BigDecimal)obj;
				int number = bd.intValue();

				return number;
			}
			
			return -1;
		} catch (Exception e) {
			if (log) Log.e(TAG, e.toString());

			BigDecimal db = getBigDecimal(data, key);
			return db.longValue();
		}
	}

	/** ================================================================== **/
	//値がBigDecimal型の場合取り出す処理　Javaで使うには面倒な型なので、Int型に変換して使用する
	/** ================================================================== **/
	public static BigDecimal getBigDecimal(HashMap<String,Object> data, String key) {
		try {
			BigDecimal bd = (BigDecimal) data.get(key);

			return bd;
		} catch (Exception e) {
			if (log) Log.e(TAG,"catch error ==================================================");
			if (log) Log.e(TAG, e.toString());

			return null;
		}
	}
	
	public static int getObjectToInteger(Object obj){
		try {
			int number = (Integer) obj;
			
			return number;
		} catch (Exception e) {
			try {
				BigDecimal bd = (BigDecimal) obj;
				int number = bd.intValue();

				return number;
			} catch (Exception e2) {
				if (log) Log.e(TAG,"文字からBigDecimal型変換失敗==========");
				if (log) Log.e(TAG, e2.toString());
				
				
				return -1;
			}
		}
	}
	/** ================================================================== **/
	//値がboolean型の場合取り出す処理
	/** ================================================================== **/
	public static boolean getBoolean(HashMap<String,Object> data, String key) {
		try {
			boolean flag = (Boolean) data.get(key);
			return flag;
		} catch (Exception e) {
			try {
				//一度失敗した場合は、念のため文字列で整合性を確認する。
				//文字列の場合はログに残す
				String text = (String) data.get(key);
				
				if (text.equals("true") || text.equals("TRUE")|| text.equals("True")) {
					return true;
				} else if (text.equals("false") || text.equals("FALSE")|| text.equals("False")) {
					return false;
				} else {
					if (log) Log.e(TAG, "文字列です");
					if (log) Log.e(TAG, "text : " + text);
					return false;
				}
			} catch (Exception e2) {
				if (log) Log.e(TAG,"Exception 文字からBoolean型変換失敗==========");
				if (log) Log.e(TAG, e2.toString());
				return false;
			}
		}
	}

	/** ================================================================== **/
	//値がboolean型（false）もしくはそれ以外の型(true)の場合取り出す処理
	/** ================================================================== **/
	public static boolean getBoolean_or_Text(HashMap<String,Object> data, String key) {
		try {
			Object object = data.get(key);

			if (object == null) {
				if (log) Log.e(TAG, "中身がありません");
				return false;
			}

			String text = (String) data.get(key);
			
			if (text == null || text.equals("false") || text.equals("FALSE")|| text.equals("False")) {
				return false;
			} else {
				if (log) Log.e(TAG, "false以外の型です");
				if (log) Log.e(TAG, "text : " + text);
				return true;
			}
		} catch (Exception e2) {
			if (log) Log.e(TAG,"Exception Boolean型からString型変換失敗==================");
			if (log) Log.e(TAG, e2.toString());

			return true;
		}
	}

	/** ================================================================== **/
	//値がHashMap<String,Object>型の場合取り出す処理
	/** ================================================================== **/
	public static HashMap<String,Object> getHash(HashMap<String,Object> data, String key) {

		try {
			HashMap<String,Object> text = (HashMap<String,Object>) data.get(key);

			if (text == null)text = new HashMap<String,Object>();

			return text;
		} catch (Exception e2) {
			if (log) Log.e(TAG,"Exception 文字かHashMap型変換失敗==========");
			if (log) Log.e(TAG, e2.toString());

			return new HashMap<String,Object>();
		}
		// if(log)Log.w(TAG,"getHash============================");
		// if(log)Log.w(TAG,"キー:" + key);
		// if(log)Log.w(TAG,"値:" + text);

	}

	/** ================================================================== **/
	//値がArrayList<HashMap<String,Object>>型の場合取り出す処理
	/** ================================================================== **/
	public static ArrayList<HashMap<String,Object>> getArrayHash(HashMap<String,Object> data, String key) {

		try {
			ArrayList<HashMap<String,Object>> text = (ArrayList<HashMap<String,Object>>) data.get(key);

			if (text == null)text = new ArrayList<HashMap<String,Object>>();
			// if(log)Log.w(TAG,"getArrayHash============================");
			// if(log)Log.w(TAG,"キー:" + key);
			// if(log)Log.w(TAG,"値:" + text);
			return text;
		} catch (Exception e2) {
			//if (log)Log.e(TAG,"Exception 文字からArrayList型変換失敗==========");
			//if (log)Log.e(TAG, e2.toString());
			
			return new ArrayList<HashMap<String,Object>>();
		}

	}

	/** ================================================================== **/
	//値がArrayList<Object>型の場合取り出す処理
	/** ================================================================== **/
	public static ArrayList<Object> getArray(HashMap<String,Object> data, String key) {
		try {
			ArrayList<Object> list = (ArrayList<Object>) data.get(key);
			if (list == null)list = new ArrayList<Object>();

			if(log) Log.w(TAG,"getArray============================");
			if(log) Log.w(TAG,"キー:" + key);
			if(log) Log.w(TAG,"値:" + list);

			return list;
		} catch (Exception e) {
			if(log) Log.e(TAG,"Exception ArrayList<Object>への変換変換失敗==========");
			if(log) Log.e(TAG,e.toString());
			
			Log.e(TAG,"Exception ArrayList<Object>への変換変換失敗==========");
			Log.e(TAG, e.toString());
			
			
			return new ArrayList<Object>();
		}

	}
	
	/** ================================================================== **/
	//JsonテキストをHashMap<String,Object>にデコードする
	/** ================================================================== **/
	public static HashMap<String,Object> getDecode(String result) {
		HashMap<String,Object> data = new HashMap<String,Object>();
		try {
			JSONObject json = new JSONObject(result);
			data = getHash(json);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return data;
	}

	private static HashMap<String, Object> getHash(JSONObject json) throws JSONException {
		HashMap<String,Object> data = new HashMap<String,Object>();

		Iterator<String> keyList = json.keys();
		for (Iterator<String> it = keyList; it.hasNext(); ) {
			String key = it.next();
			Object obj = json.get(key);

			if (obj instanceof String) {
				data.put(key, (String) obj);
			} else if (obj instanceof Integer) {
				data.put(key, (Integer) obj);
			} else if (obj instanceof Float) {
				data.put(key, (Float) obj);
			} else if (obj instanceof Double) {
				data.put(key, (Double) obj);
			} else if (obj instanceof Long) {
				data.put(key, (Long) obj);
			} else if (obj instanceof BigDecimal) {
				data.put(key, (BigDecimal) obj);
			} else if (obj instanceof HashMap) {
				data.put(key, (HashMap) obj);
			} else if (obj instanceof Map) {
				data.put(key, (Map) obj);
			} else if (obj instanceof ArrayList) {
				data.put(key, (ArrayList) obj);
			} else if (obj instanceof List) {
				data.put(key, (List) obj);
			} else if (obj instanceof JSONObject) {
				data.put(key, getHash((JSONObject) obj));
			} else if (obj instanceof JSONArray) {
				data.put(key, getArray((JSONArray) obj));
			}
		}
		return data;
	}

	private static ArrayList<Object> getArray(JSONArray jsonArray) throws JSONException {
		ArrayList<Object> list = new ArrayList<Object>();

		for(int i=0;i<jsonArray.length();i++) {
			Object obj = jsonArray.get(i);

			if (obj instanceof String) {
				list.add((String) obj);
			} else if (obj instanceof Integer) {
				list.add((Integer) obj);
			} else if (obj instanceof Float) {
				list.add((Float) obj);
			} else if (obj instanceof Double) {
				list.add((Double) obj);
			} else if (obj instanceof Long) {
				list.add((Long) obj);
			} else if (obj instanceof BigDecimal) {
				list.add((BigDecimal) obj);
			} else if (obj instanceof HashMap) {
				list.add((HashMap) obj);
			} else if (obj instanceof Map) {
				list.add((Map) obj);
			} else if (obj instanceof ArrayList) {
				list.add((ArrayList) obj);
			} else if (obj instanceof List) {
				list.add((List) obj);
			} else if (obj instanceof JSONObject) {
				list.add(getHash((JSONObject) obj));
			} else if (obj instanceof JSONArray) {
				list.add(getArray((JSONArray) obj));
			}
		}

		return list;
	}
}
