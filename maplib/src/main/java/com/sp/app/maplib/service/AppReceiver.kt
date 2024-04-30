package com.sp.app.maplib.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AppReceiver : BroadcastReceiver() {

    var time:Long = 0

    override fun onReceive(context: Context, intent: Intent?) {


        var id = 123456789
        //var action = Constant.INTENT_STOP_ALARM
        intent?.let{
            //id = intent.extras.getInt("id")
            //action = intent.action
        }


        //Log.i("AlarmStopReceiver","action:" + action)

        //Serviceを起動する
        val serviceIntent = Intent(context, AppService::class.java)
        /**
        if(action.equals(Constant.INTENT_END_ALARM)){
            serviceIntent.putExtra("event", Constant.Companion.ServiceCommand.ALARM_END.command)
        }else {
            serviceIntent.putExtra("event", Constant.Companion.ServiceCommand.ALARM_STOP.command)
        }
        */
        serviceIntent.putExtra("id", id)

        context?.let{
            Log.i("AlarmStopReceiver","context有効 startService実行")
            context?.startService(serviceIntent)
        }

    }

}