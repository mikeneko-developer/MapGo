package com.sp.app.mapgo.ui.base

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer


fun <T> LiveData<T>.observeOnChanged(owner: LifecycleOwner, observer: Observer<T>) : Unit {
    var prev : T? = null
    this.observe(owner, Observer<T> {
        if (!(prev?.equals(it) ?: false)) {
            observer.onChanged(it)
        }
        prev = it
    })
}
fun <T> LiveData<T>.observeOnChangedForever(observer: Observer<T>) : Unit {
    var prev : T? = null
    this.observeForever(Observer<T> {
        if (!(prev?.equals(it) ?: false)) {
            observer.onChanged(it)
        }
        prev = it
    })
}

/************************************************************
 *  Extention Method
 */

/**
 * Runs a FragmentTransaction, then calls commit().
 */
private inline fun FragmentManager.transact(action: FragmentTransaction.() -> Unit) {
    beginTransaction().apply {
        action()
    }.commit()
}


fun AppCompatActivity.hideFragmentEnabled(fragment: Fragment){
    android.util.Log.i("TEST_LOG","ActivityNavigator >> hideFragment()")
    if(fragment.isAdded) {
        supportFragmentManager.transact {
            hide(fragment)
            fragment.onPause()
        }
    }
}


fun AppCompatActivity.showFragmentEnabled(fragment: Fragment){
    android.util.Log.i("TEST_LOG","ActivityNavigator >> showFragment()")
    if(fragment.isAdded) {
        supportFragmentManager.transact {
            show(fragment)
            fragment.onResume()
        }
    }
}

/************************************************************
 *  フラグメントの設定用宣言処理
 */


/**
 * The `fragment` is added to the container view with tag. The operation is
 * performed by the `fragmentManager`.
 */
fun AppCompatActivity.addFragmentToActivity(fragment: Fragment, frameId: Int, tag: String?) {

    android.util.Log.i("TEST_LOG","ActivityNavigator >> addFragmentToActivity( "+tag+" ) isAdded:" + fragment.isAdded)
    if(!fragment.isAdded) {
        supportFragmentManager.transact {
            add(frameId, fragment, tag)
        }
    }
}

/**
 * The `fragment` is replace to the container view with tag. The operation is
 * performed by the `fragmentManager`.
 */
fun AppCompatActivity.replaceFragmentToActivity(fragment: Fragment, frameId: Int, tag: String?) {

    android.util.Log.i("TEST_LOG","AppCompatActivity.replaceFragmentToActivity() >> isAdded : " + fragment.isAdded)
    if(!fragment.isAdded) {
        supportFragmentManager.transact {
            replace(frameId, fragment, tag)
        }
    }
}





/**
 * The `fragment` is added to the container view with tag. The operation is
 * performed by the `fragmentManager`.
 */
fun AppCompatActivity.addToBackStackFragmentToActivity(fragment: Fragment, frameId: Int, tag: String?)
{
    if(!fragment.isAdded) {
        supportFragmentManager.transact {
            add(frameId, fragment, tag)
            addToBackStack(tag)
        }
    }
}

/**
 * The `fragment` is replace to the container view with tag. The operation is
 * performed by the `fragmentManager`.
 */
fun AppCompatActivity.replaceToBackStackFragmentToActivity(fragment: Fragment, frameId: Int, tag: String?) {
    android.util.Log.i("TEST_LOG","AppCompatActivity.replaceFragmentToActivity() >> isAdded : " + fragment.isAdded)

    if(!fragment.isAdded) {
        supportFragmentManager.transact {
            replace(frameId, fragment, tag)
            addToBackStack(tag)
        }
    }
}


@SuppressLint("LongLogTag")
fun AppCompatActivity.popBackStackImmediate(): Boolean{

    if(supportFragmentManager.backStackEntryCount == 0){
        return false
    }
    try{
        return supportFragmentManager.popBackStackImmediate()
    }catch(e: Exception){
        android.util.Log.e("BaseActivityCompatActivityExt","error : " + e.toString())
        return false
    }
}

fun AppCompatActivity.popBackStack() {
    supportFragmentManager.popBackStack()
}

fun AppCompatActivity.removeFragment(fragment : Fragment?) {
    fragment?.let {
        if (fragment.isAdded) {
            supportFragmentManager.transact {
                remove(fragment)
            }
        }
    }
}

/**
 * ダイアログの表示管理用
 */
fun AppCompatActivity.showDialogFragmentWithTargetFragmentShow(dialog: DialogFragment, tag: String?, fragment: Fragment, requestCode: Int) {
    if (!dialog.isAdded) {
        dialog.setTargetFragment(fragment, requestCode)
        dialog.show(supportFragmentManager, tag)
    }
}

fun AppCompatActivity.showDialogFragmentWithTargetFragmentShow(dialog: DialogFragment, fragment: Fragment, requestCode: Int) {
    if (!dialog.isAdded) {
        dialog.setTargetFragment(fragment, requestCode)
        dialog.show(supportFragmentManager, "showDialog")
    }
}

fun AppCompatActivity.clearBackStack(){
    val stackCount = supportFragmentManager.backStackEntryCount
    for (i in 0 until stackCount) {
        supportFragmentManager.popBackStack()
    }
}

