package net.mikemobile.navi.ui.navi.favorite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import net.mikemobile.databindinglib.base.BaseFragment
import net.mikemobile.databindinglib.base.BaseNavigator

import androidx.databinding.DataBindingUtil
import org.koin.android.viewmodel.ext.android.viewModel
import net.mikemobile.navi.R
import net.mikemobile.navi.databinding.*
import net.mikemobile.navi.ui.dialog.ListDialog
import net.mikemobile.navi.util.Constant

interface FavoriteEditFragmentNavigator: BaseNavigator {
    fun onError(error:String)
    fun onClickOpenDialog()
}

class FavoriteEditFragment: BaseFragment(),
    FavoriteEditFragmentNavigator {

    private val viewModel: FavoriteEditViewModel by viewModel()

    companion object {
        const val TAG = "FavoriteEditFragment"
        fun newInstance() = FavoriteEditFragment()
    }

    // ---------------------------------------------------------------------------------------------
    //データバインディングを有効にする
    override fun isDataBinding(): Boolean{
        return true
    }

    //
    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{

        val binding = DataBindingUtil.inflate<FragmentFavoriteEditBinding>(inflater, R.layout.fragment_favorite_edit, container,false)
        val view = binding.root
        viewModel.navigator = this
        binding.viewmodel = viewModel

        binding.lifecycleOwner = this

        /////////////////////////////////////

        viewModel.initialize()


        return view
    }

    //
    override fun onActivityCreate(savedInstanceState: Bundle?) {

    }

    override fun onResume() {
        super.onResume()
        viewModel.resume(this)
    }

    override fun onPause() {
        super.onPause()
        viewModel.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.destroy()
    }

    /**
    override fun onBack() {
        Toast.makeText(context,"onBack",Toast.LENGTH_SHORT).show()
        activityNavigator.onBack()
    }
    */


    //
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }

    // ---------------------------------------------------------------------------------------------
    // BaseNavigatorのメソッド
    override fun onCloseFragment() {
        activityNavigator.onBack()
    }

    override fun onError(error:String){
        Toast.makeText(context,"" + error, Toast.LENGTH_SHORT).show()
    }

    override fun onClickOpenDialog() {
        openDialog()
    }

    // ----------------

    fun openDialog() {
        activityNavigator.showDialogFragmentWithTargetFragment(ListDialog.TAG, this,
            Constant.Companion.DIALOG_ID.SELECT_POINT_MENU.id, Bundle())
    }
}