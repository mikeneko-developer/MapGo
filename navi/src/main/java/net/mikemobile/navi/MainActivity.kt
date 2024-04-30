package net.mikemobile.navi

import android.app.PictureInPictureParams
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.util.Rational
import android.view.View.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.WindowManager
import android.widget.FrameLayout
import net.mikemobile.databindinglib.BaseActivityApplication
import net.mikemobile.databindinglib.base.BaseActivity
import net.mikemobile.databindinglib.base.BaseFragmentFactory
import net.mikemobile.databindinglib.base.popBackStackImmediate
import net.mikemobile.navi.repository.MapRepository
import net.mikemobile.navi.service.OnServiceListener
import net.mikemobile.navi.service.RoboAppService
import net.mikemobile.navi.system.DataBindingApplication
import net.mikemobile.navi.system.FragmentFactory
import net.mikemobile.navi.ui.map.MapFragment
import net.mikemobile.navi.ui.menu.MapMenuFragment
import net.mikemobile.navi.ui.navi.MapRouteFragment
import net.mikemobile.navi.ui.navi.MapTopFragment
import org.koin.android.ext.android.inject


class MainActivity : BaseActivity() {

    val TAG = "MainActivity"

    var handler: Handler = Handler()
    var first = false

    private val mapRepository: MapRepository by inject()

    override fun setActivityApplication(): BaseActivityApplication {
        return application as DataBindingApplication
    }

    override fun onFragmentFactory(): BaseFragmentFactory {
        return FragmentFactory().getInstance()
    }

    override fun onCreateView(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        first = true


        startService()

        val mainView = findViewById<FrameLayout>(net.mikemobile.databindinglib.R.id.main_frame)
        mainView.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mainView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (first) {
                    Log.i(TAG, "addOnGlobalLayoutListener")
                    // 地図を表示するViewのサイズを一回だけ取得
                    val width = mainView.width
                    val height = mainView.height

                    mapRepository.windowWidth = width
                    mapRepository.windowHeight = height

                    setView()
                }
                first = false
            }
        })

    }

    private fun setView() {
        replaceFragmentInContentFrame(
            MapTopFragment.TAG,
            BaseActivity.DEFAULT_CONTENT_VIEW_MAIN,
            null
        )
        replaceFragmentInContentFrame(
            MapMenuFragment.TAG,
            BaseActivity.DEFAULT_CONTENT_VIEW_TAB,
            null
        )
        replaceFragmentInContentFrame(
            MapFragment.TAG,
            BaseActivity.DEFAULT_CONTENT_VIEW_BACKGROUND,
            null
        )
    }

    override fun onDestroy(){
        super.onDestroy()

        stopService()
    }

    override fun onBack() {
        android.util.Log.i("TEST_LOG", "BaseActivity >> onBack()")
        val list = ArrayList<String>()
        list.add(MapRouteFragment.TAG)


        if (!popBackStackImmediate()) {
            finish()
        }

        //
    }

    /** ================================================================================= */
    // Service関連
    private var serviceBinder: RoboAppService.LocalBinder? = null
    private var listener = object: OnServiceListener {
        override fun onUndindService() {
            unBindService()
        }

    }

    fun stopService() {
        val serviceIntent = Intent(this, RoboAppService::class.java)
        stopService(serviceIntent)
    }

    fun startService(){

        val serviceIntent = Intent(this, RoboAppService::class.java)
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

    fun startPictureInPicture() {
        /**
        val args = PictureInPictureArgs()

        val actions: ArrayList<RemoteAction> = ArrayList()
        val intent: PendingIntent = PendingIntent.getBroadcast(
            this@MainActivity,
            REQUEST_CODE_ACTION_HOGE_A, Intent(ACTION_HOGE).putExtra(EXTRA_CONTROL_TYPE, CONTROL_TYPE_A), 0
        )
        val icon: Icon = Icon.createWithResource(this@MainActivity, R.drawable.image_robo)
        actions.add(RemoteAction(icon, "title", "content description", intent))

        args.setActions(actions)
        enterPictureInPictureMode(args)
        */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder().apply {
                setAspectRatio(Rational(4, 3))
            }.build()

            enterPictureInPictureMode(params)
        } else {

        }

    }
//
//    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
//        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
//        if (isInPictureInPictureMode) {
//            //text.text = "small"
//            setFullScreen(true)
//        } else {
//            //text.text = "normal"
//            setFullScreen(false)
//        }
//    }

    private fun setFullScreen(on: Boolean) {
        window.decorView.systemUiVisibility = if (on) {
            SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    SYSTEM_UI_FLAG_FULLSCREEN or
                    SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        } else {
            SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }



    /**
     * ServiceConnection By NaviService
     */
    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // サービスの準備ができたか確認
            val binder = service as RoboAppService.LocalBinder
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

    fun unBindService() {
        try{
            if(serviceBinder != null) {
                unbindService(mConnection)
            }
        }catch (e: Exception){

        }
    }

}
