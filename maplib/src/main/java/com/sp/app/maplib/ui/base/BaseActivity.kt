package com.sp.app.maplib.ui.base

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log

abstract class BaseActivity : AppCompatActivity() {

    companion object {
        const val BASE_TAG: String = "BaseActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }
    /*************************************************************/
    // Override出来るように置いているメソッド


    /*************************************************************/
    override fun onDestroy(){
        super.onDestroy()
    }

    open fun onBack() {
        android.util.Log.i("TEST_LOG","BaseActivity >> onBack()")

    }

    open fun onFinish() {
        super.finish()
    }


    override fun onResume() {
        super.onResume()
        android.util.Log.i("TEST_LOG3","BaseActivity >> onResume()")

    }
    override fun onPause() {
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)

        Log.i(BASE_TAG + " ADD_MAKER","onActivityResult")
    }

    /*************************************************************/
    //Override用メソッド

    open fun onActivityResulted(requestCode: Int, resultCode: Int, data: Intent?){}


    fun hideFragment(fragment: BaseFragment) {
        hideFragmentEnabled(fragment)
    }

    fun showFragment(fragment: BaseFragment) {
        showFragment(fragment)
    }

    fun removeFragment(fragment: BaseFragment) {
        showFragmentEnabled(fragment)
    }

    fun addFragment(fragment: BaseFragment, layoutId: Int, tag: String) {
        addFragmentToActivity(fragment, layoutId, tag)
    }

    fun replaceFragment(fragment: BaseFragment, layoutId: Int, tag: String) {
        replaceFragmentToActivity(fragment, layoutId, tag)
    }



}
