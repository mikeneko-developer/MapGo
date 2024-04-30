package net.mikemobile.navi.ui.robos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import net.mikemobile.databindinglib.base.BaseFragment
import net.mikemobile.databindinglib.base.BaseNavigator

import androidx.databinding.DataBindingUtil
import net.mikemobile.databindinglib.base.BaseActivity
import org.koin.android.viewmodel.ext.android.viewModel
import net.mikemobile.navi.R
import net.mikemobile.navi.databinding.FragmentMapTopBinding
import net.mikemobile.navi.databinding.FragmentTalkBinding
import net.mikemobile.navi.ui.dialog.ListDialog
import net.mikemobile.navi.ui.dialog.SelectButtonDialog
import net.mikemobile.navi.ui.menu.MapMenuFragment
import net.mikemobile.navi.ui.menu.MenuFragment
import net.mikemobile.navi.util.Constant

interface TalkFragmentNavigator: BaseNavigator {
    fun onError(error:String)
}

class TalkFragment: BaseFragment(),
    TalkFragmentNavigator {

    private val viewModel: TalkViewModel by viewModel()

    companion object {
        const val TAG = "TalkFragment"
        fun newInstance() = TalkFragment()
    }

    // ---------------------------------------------------------------------------------------------
    //データバインディングを有効にする
    override fun isDataBinding(): Boolean{
        return true
    }

    //
    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{

        val binding = DataBindingUtil.inflate<FragmentTalkBinding>(inflater, R.layout.fragment_talk, container,false)
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

    //
    override fun onBack() {

    }

    // ---------------------------------------------------------------------------------------------
    // BaseNavigatorのメソッド
    override fun onCloseFragment() {

    }

    override fun onError(error:String){
        Toast.makeText(context,"" + error, Toast.LENGTH_SHORT).show()
    }


}