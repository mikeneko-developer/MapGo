package com.sp.app.mapgo.ui.base

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment


abstract class BaseFragment : Fragment() {

    inline fun <T : View> T.afterMeasured(crossinline f: T.() -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    f()
                }
            }
        })
    }

    abstract fun onActivityCreate(savedInstanceState: Bundle?)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return onCreateViewBinding(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        android.util.Log.i("BaseFragment","onActivityCreated()")
        onActivityCreate(savedInstanceState)
    }

    override fun onStart(){
        super.onStart()
        android.util.Log.i("BaseFragment","onStart()")
    }
    override fun onStop(){
        super.onStop()
        android.util.Log.i("BaseFragment","onStop()")
    }
    override fun onDestroy(){
        super.onDestroy()
        android.util.Log.i("BaseFragment","onDestroy()")
    }

    /**
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext = context
    }*/


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    open fun onBack() {
        android.util.Log.i("BaseFragment","onBack()")
    }

    open fun onContentView(): Int {
        return -1
    }

    fun onCreateView(savedInstanceState: Bundle?) {

    }

    open fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        return null
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun setupLayout(savedInstanceState: Bundle?){

    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
}