package net.mikemobile.databindinglib.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import net.mikemobile.databindinglib.BackStackHandler
import net.mikemobile.databindinglib.BackStackHandlerListener


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




/************************************************************
 *  各ビューを新規生成もしくは既存のFragmentを呼び出すための処理
 */

/**
 * Find or create fragment instance
 * @return BaseFragment
 */
fun AppCompatActivity.findOrCreateViewFragment(fragmentFactory: BaseFragmentFactory, tag: String, bundle: Bundle?) : BaseFragment {

    if (supportFragmentManager.findFragmentByTag(tag) as? BaseFragment != null) {
        Log.i(" CREATE_FRAGMENT", "FragmentManagerにデータあり + " + tag)
        return supportFragmentManager.findFragmentByTag(tag) as BaseFragment
    } else {
        Log.i(" CREATE_FRAGMENT", "Fragmentを新規に生成 + " + tag)
        return fragmentFactory.create(tag, bundle)
    }
}

/**
 * Find or create dialogFragment instance
 * @return DialogFragment
 */
fun AppCompatActivity.findOrCreateDialogFragment(fragmentFactory: BaseFragmentFactory, tag: String?, bundle: Bundle?) : DialogFragment {
    val fragment = supportFragmentManager.findFragmentByTag(tag) as? DialogFragment
        ?: fragmentFactory.createDialog(tag!!, bundle)

    fragment.arguments = bundle

    return fragment
}


/**
 * Find fragment from supportFragmentManager
 * @return BaseFragment?
 */
fun AppCompatActivity.findFragmentByTag(tag: String?) : BaseFragment?{
    val fragment = supportFragmentManager.findFragmentByTag(tag)
    return fragment as? BaseFragment
}

/**
 * Find dialog fragment from supportFragmentManager
 * @return DialogFragment?
 */
fun AppCompatActivity.findDialogFragmentByTag(tag: String?) : DialogFragment?{
    val fragment = supportFragmentManager.findFragmentByTag(tag)
    return fragment as? DialogFragment
}

/**
 * Find fragment from supportFragmentManager
 * @return BaseFragment?
 */
fun AppCompatActivity.findFragmentById(id : Int) : BaseFragment?{
    val fragment = supportFragmentManager.findFragmentById(id)
    return fragment as? BaseFragment
}


fun AppCompatActivity.hideFragment(fragment: Fragment){
    android.util.Log.i("TEST_LOG","ActivityNavigator >> hideFragment()")
    if(fragment.isAdded) {
        supportFragmentManager.transact {
            hide(fragment)
            fragment.onPause()
        }
    }
}


fun AppCompatActivity.showFragment(fragment: Fragment){
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




// バックスタック制御用Handlerクラス
private var backStackHandler = BackStackHandler()

// onResume()が呼ばれた時、実行するメソッド
fun AppCompatActivity.onBackStackResume(listener: BackStackHandlerListener){

    backStackHandler.setOnBackStackHandlerListener(listener)

    // 格納されているイベントを開放する
    backStackHandler.resume()
}
// onPause()が呼ばれた時、実行するメソッド
fun AppCompatActivity.onBackStackPause(){
    // フラグをpause状態に変更する
    backStackHandler.pause()
}
// バックスタックエラーを起こさずFragmentを実行するためのメソッド
fun AppCompatActivity.safetyRun(id: Int, bundle: Bundle){
    backStackHandler.safetyRun(id,bundle)
}
// バックスタックエラーを起こさずFragmentを実行するためのメソッド（ターゲットとなるFragmentを指定する場合はこちらを使用する）
fun AppCompatActivity.safetyRun(id: Int, bundle: Bundle, fragment: Fragment){
    backStackHandler.safetyRun(id, bundle, fragment)
}