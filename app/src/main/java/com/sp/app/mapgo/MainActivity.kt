package com.sp.app.mapgo

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import com.sp.app.mapgo.ui.map.MapFragment
import com.sp.app.mapgo.ui.viewmodel.MainViewModel
import com.sp.app.maplib.service.AppService
import com.sp.app.maplib.service.OnServiceListener
import com.sp.app.maplib.ui.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.ref.WeakReference


class MainActivity : BaseActivity() {


    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //画面の向きを縦に固定
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        setContentView(R.layout.activity_main)

        addFragment(MapFragment(), R.id.content_background, "MapFragment")

        startService()
    }

    override fun onResume(){
        super.onResume()
    }

    override fun onPause(){
        super.onPause()
    }



    override fun onDestroy(){
        super.onDestroy()

        stopService()
    }

    /** ================================================================================= */
    // Service関連
    private var serviceBinder: AppService.LocalBinder? = null
    private var listener = object: OnServiceListener {
        override fun onUndindService() {
            unBindService()
        }

    }

    fun stopService() {
        val serviceIntent = Intent(this, AppService::class.java)
        stopService(serviceIntent)
    }

    fun startService(){

        val serviceIntent = Intent(this, AppService::class.java)
        //serviceIntent.putExtra("event", Constant.Companion.ServiceCommand.CHECK.command)

        //////////////////////////////////
        // Start Navi Service

        if(serviceBinder == null) {
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }

            //////////////////////////////////
            //bind Service
            bindService(serviceIntent, mConnection, 0)
            //////////////////////////////////

        }else {
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
    }

    fun unBindService() {
        try{
            if(serviceBinder != null) {
                unbindService(mConnection)
            }
        }catch (e: Exception){

        }
    }
    /**
     * ServiceConnection By NaviService
     */
    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // サービスの準備ができたか確認
            val binder = service as AppService.LocalBinder
            binder.setListener(listener)

            // フィールド変数にバインダーを置く
            serviceBinder = binder

            //listener = binder.getListener()
            //binder.setListener(toAlarmListener)

            //unBindService()
        }
        // Called when the connection with the service disconnects unexpectedly
        override fun onServiceDisconnected(className: ComponentName) {
            //serviceStateCallback?.onServiceDisconnected()
            //listener = null
            serviceBinder?.setListener(null)
            serviceBinder = null
        }
    }
}