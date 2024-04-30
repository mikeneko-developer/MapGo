package net.mikemobile.navi.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import net.mikemobile.navi.core.gps.SimpleLocationListener;
import net.mikemobile.navi.core.gps.onMyLocationListener;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MyLocation {
	static String TAG = "MyLocation";
	private Context context;
	public MyLocation(Context context){
		this.context = context;
		isLocate = false;
		setLocate(context);
	}

	private boolean enableGPS = true;
	public MyLocation(Context context, boolean flag){
		this.context = context;
		isLocate = false;
		enableGPS = flag;
		setLocate(context);
	}

	private LocationManager mLocationManager;
	private float log_gps_lat;
	private float log_gps_lon;
	private Criteria criteria = new Criteria();

	public static int low_accuracy = Criteria.ACCURACY_LOW;
	public static int high_accuracy = Criteria.ACCURACY_HIGH;
	public static int fine_accuracy = Criteria.ACCURACY_FINE;
	public static int coa_accuracy = Criteria.ACCURACY_COARSE;
	public static int mid_accuracy = Criteria.ACCURACY_MEDIUM;

	public static int low_power = Criteria.POWER_LOW;
	public static int mid_power = Criteria.POWER_MEDIUM;
	public static int high_power = Criteria.POWER_HIGH;

	private boolean update = false;
	public boolean isLocate = false;
	private int wait = 15000;
	private int wait_long = 500;

	private void setLocate(Context context){
		if(mLocationManager == null){
			mLocationManager = (LocationManager)context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
		}
	}
	public boolean checkGPS(){
		if(mLocationManager == null){
			return false;
		}


		String provider = LocationManager.NETWORK_PROVIDER;
		//provider = LocationManager.GPS_PROVIDER;
		//provider = LocationManager.PASSIVE_PROVIDER;

		return mLocationManager.isProviderEnabled(provider);
	}

	public MyLocation setGPSSetup(int power){
		if(!checkGPS()){
			return this;
		}
		//GPS情報をリアル取得する。　ユーザー側の設定でGPSで取るかネットワーク（基地局情報）で取るか判断する
        //位置情報サービスの要求条件をピックアップする
        //速度、電力消費などから適切な位置情報サービスを選択する

		try{
			criteria.setAccuracy(Criteria.ACCURACY_HIGH); // Accuracyを指定(高精度)
			Log.i(TAG, "accuracy:Criteria.ACCURACY_HIGH");
		}catch(Exception e){
			Log.e(TAG, "GPSの精度設定失敗 accuracy:Criteria.ACCURACY_HIGH");
			Log.e(TAG, e.toString());

			try{
				criteria.setAccuracy(Criteria.ACCURACY_FINE); // Accuracyを指定(高精度)
				Log.i(TAG, "accuracy:Criteria.ACCURACY_FINE");
			}catch(Exception e2){
				Log.e(TAG, "GPSの精度設定失敗 accuracy:Criteria.ACCURACY_FINE");
				Log.e(TAG, e2.toString());

				try{
					criteria.setAccuracy(Criteria.ACCURACY_MEDIUM); // Accuracyを指定(高精度)
					Log.i(TAG, "accuracy:Criteria.ACCURACY_MEDIUM");
				}catch(Exception e3){
					Log.e(TAG, "GPSの精度設定失敗 accuracy:Criteria.ACCURACY_MEDIUM");
					Log.e(TAG, e3.toString());

					try{
						criteria.setAccuracy(Criteria.ACCURACY_COARSE); // Accuracyを指定(高精度)
						Log.i(TAG, "accuracy:Criteria.ACCURACY_COARSE");
					}catch(Exception e4){
						Log.e(TAG, "GPSの精度設定失敗 accuracy:Criteria.ACCURACY_COARSE");
						Log.e(TAG, e4.toString());

						try{
							criteria.setAccuracy(Criteria.ACCURACY_LOW); // Accuracyを指定(高精度)
							Log.i(TAG, "accuracy:Criteria.ACCURACY_LOW");
						}catch(Exception e5){
							Log.e(TAG, "GPSの精度設定失敗 accuracy:Criteria.ACCURACY_LOW");
							Log.e(TAG, e5.toString());
						}
					}
				}
			}
		}



        try{
        	if(power == high_power){
                criteria.setPowerRequirement(Criteria.POWER_HIGH);// PowerRequirementを指定(高消費電力)
            }else if(power == mid_power){
            	criteria.setPowerRequirement(Criteria.POWER_MEDIUM);// PowerRequirementを指定(中消費電力)
            }else {
            	criteria.setPowerRequirement(Criteria.POWER_LOW);// PowerRequirementを指定(低消費電力)
            }
        }catch(Exception e){
        	Log.e(TAG, "GPSのバッテリー消費モード設定失敗 power:" + power);
        	Log.e(TAG, e.toString());
        }
		Log.i(TAG, "初期設定終了");

        return this;
	}

	public void start(){
		if(isLocate){
			return;
		}

		isLocate = true;

		//GPSが利用可能か？
        if(checkGPS()){
        	//利用可能
        	//使える中で最も条件にヒットする位置情報サービスを取得する
            String bestProvider_ = mLocationManager.getBestProvider(criteria, true);

            if(bestProvider_ != null){
                //位置更新の際のリスナーを登録。省電力のために通知の制限をする。
                //最小で15000msec周期、最小で1mの位置変化の場合(つまり、どんなに変化しても15000msecのより短い間隔では通知されず、1mより小さい変化の場合は通知されない。)
            	mLocationManager.requestLocationUpdates(bestProvider_, wait, 1, gpslistener);

            }
        }else {
        	//利用不可
        }

        if(mLocationManager == null){
        	isLocate = false;
            changeLocation(-1,null);
			return;
		}

        //ネットワークから位置情報を取得が利用可能か？
        if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){//

        }

        startSaveLocation();

	}

	@SuppressLint("MissingPermission")
	private void saveLovation(){
		update = true;

		String getType = "";


        // Criteriaを変更することで，各種設定変更可能
        String bs = mLocationManager.getBestProvider(criteria, true);
        Location locate = null;

        if(bs != null){
			getType = "" + bs;

			//Log.i(TAG, "Log ロケーション情報を取得");
            locate = mLocationManager.getLastKnownLocation(bs);


			if(locate == null){
				// GPSで取得してみる
				locate = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

				if(locate != null){
					getType = "GPS";
					//Log.i(TAG, "Log GPSでロケーション情報を取得");
				}else {

				}
			}

            if(locate == null){
                //　無線測位で取得してみる
                locate = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if(locate != null){
					getType = "無線測位";
                	//Log.i(TAG, "Log 無線測位でロケーション情報を取得");
                }
            }
            
            if(locate != null){ // 現在地情報取得成功
                // 緯度の取得
            	
            	float log_gps_lat1 = (float) locate.getLatitude();
            	float log_gps_lon1 = (float) locate.getLongitude();
            	
            	if(log_gps_lat1 != log_gps_lat 
            			|| log_gps_lon1 != log_gps_lon){
            		
            		log_gps_lat = log_gps_lat1;
            		log_gps_lon = log_gps_lon1;
					Log.e(TAG, "Log " + getType);

					if(getType.equals("無線測位")){
						changeLocation(1,locate);
					}else {
						changeLocation(0,locate);
					}

            	}else {
					//Log.e(TAG, "Log 位置情報が前回と同じです");
				}
            } else {
            	/* 現在地情報取得失敗処理 */
                Log.e(TAG, "Log 位置情報取得に失敗しました。");
            	locate = new Location("");
            	locate.setLatitude(log_gps_lat);
            	locate.setLongitude(log_gps_lon);
                
            }
        }else{
            /* 位置情報取得可能な設定になっていない場合、設定画面を開く */
            Log.i(TAG, "Log 位置情報取得機能が無効です");
        	locate = new Location("");
        	locate.setLatitude(log_gps_lat);
        	locate.setLongitude(log_gps_lon);
        	
        }
        
        
	}
	
	
	private void changeLocation(int gettype, Location location){
		
		if(location == null || !update && gettype != 0){
			Log.i(TAG, "ロケーション情報を取得できなかった /"+ update + "/" + gettype);
			//listener.NotGPSResult();
			return;
		}
		update = false;
		
		Log.i(TAG, "Lat:" + String.valueOf(location.getLatitude()) + " Lon:" + String.valueOf(location.getLongitude()));
		
		if(gettype == 0){
			listener.onGpsLogLocation(context,location);
		}else if(gettype == 1){
			listener.onNetworkLogLocation(context,location);
		}else {
			//listener.NotGPSResult();
		}
	}
	
	private LocationListener gpslistener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
        	startSaveLocation();
			Log.v(TAG,"GPS情報取得 onLocationChanged");
        	Log.i(TAG, "緯度 Latitude:" + String.valueOf(location.getLatitude()));
        	Log.i(TAG, "経度 Longitude" + String.valueOf(location.getLongitude()));
        	Log.i(TAG, "エラーレンジ Accuracy" + String.valueOf(location.getAccuracy()));
        	Log.i(TAG, "標高 Altitude:" + String.valueOf(location.getAltitude()));
        	Log.i(TAG, "Tim:e" + String.valueOf(location.getTime()));
        	Log.i(TAG, "Speed:" + String.valueOf(location.getSpeed()));
        	Log.i(TAG, "Bearing:" + String.valueOf(location.getBearing()));

            
            //float lat = (float) location.getLatitude();
            //float lon = (float) location.getLongitude();
			listener.onGpsLocation(context,location);
            
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };
    
    public void stop(){
    	isLocate = false;
    	if (mLocationManager != null) {
    		mLocationManager.removeUpdates(gpslistener);
        }
    	stopSaveLocation();
    }
    

	private Handler handler = new Handler();
	TimerTask timerTask;
	Timer timer;
	private void startSaveLocation(){
		stopSaveLocation();
		
		timer = new Timer();
		timer = new Timer(true);

        timerTask = new TimerTask() {
            public void run() {
                handler.post(new getLocation());
            }
        };

        //int delay = 1000 * 60 * 60 * 24;
        /** ▼ 2014/3/21 追加 =============================================== **/
        //通信用データに問題がある場合は処理を実行しないよう追加
        
        /** ▲ ============================================================= **/

        //delay = Global.INTERVAL_PERIOD;
        Date now = new Date();

        timer.scheduleAtFixedRate(timerTask, now, wait_long);
        //timer.schedule(timerTask, wait_long);
	}

	private void stopSaveLocation(){
		//Toast.makeText(getContext(), "長押し中止", Toast.LENGTH_SHORT).show();
		if(timerTask != null){
			timerTask.cancel();
			timerTask = null;
		}
		
		if(timer != null){
			timer.cancel();
			timer = null;
		}
	}

	class getLocation implements Runnable {
		@Override
		public void run() {
			// 現在のプレビューをデータに変換
			saveLovation();
		}
	}
	

	/** ================== インターフェース ================== **/
    private onMyLocationListener listener = (onMyLocationListener) new SimpleLocationListener();

    public void setOnMyLocationListener(onMyLocationListener listener) {this.listener = listener;}






    
}

