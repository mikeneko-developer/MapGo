package net.mikemobile.navi.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import net.mikemobile.databindinglib.base.BaseActivity
import net.mikemobile.databindinglib.base.BaseFragment
import net.mikemobile.databindinglib.base.BaseNavigator
import net.mikemobile.navi.R
import net.mikemobile.navi.databinding.FragmentMapGuideBinding
import net.mikemobile.navi.databinding.FragmentMenuBinding
import net.mikemobile.navi.ui.map.MapFragment
import net.mikemobile.navi.ui.navi.MapTopFragment
import net.mikemobile.navi.ui.robos.RoboListFragment
import org.koin.android.viewmodel.ext.android.viewModel

interface MenuFragmentNavigator: BaseNavigator {
    fun onMenuSelect(tag: String)
}


class MenuFragment : BaseFragment(),
    MenuFragmentNavigator {

    companion object {
        const val TAG = "MenuFragment"
        fun newInstance() = MenuFragment()
    }


    private val viewModel: MenuFragmentViewModel by viewModel()

    // ---------------------------------------------------------------------------------------------
    //データバインディングを有効にする
    override fun isDataBinding(): Boolean{
        return true
    }

    //
    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{

        val binding = DataBindingUtil.inflate<FragmentMenuBinding>(inflater, R.layout.fragment_menu, container,false)
        val view = binding.root

        binding.viewmodel = viewModel

        viewModel.initialize()
        viewModel.navigator = this


        return view
    }

    //
    override fun onActivityCreate(savedInstanceState: Bundle?) {

        // 最初に表示する画面を指定
    }

    override fun onResume() {
        super.onResume()
        //viewModel.resume(this)
    }

    override fun onPause() {
        super.onPause()
        //viewModel.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        //viewModel.destroy()
    }

    override fun onMenuSelect(tag: String) {
        activityNavigator.onBack()
        activityNavigator.replaceFragmentInMainContentFrame(tag)
        if(tag.equals(MapTopFragment.TAG)){
            openMap()
        }else {
            activityNavigator.removeFragment(MapFragment.TAG)

        }

        if(tag.equals(RoboListFragment.TAG)){
            openRoboList()
        }
    }

    /**
    override fun onBack() {

    }
     */

    // ---------------------------------------------------------------------------------------------
    // BaseNavigatorのメソッド
    override fun onCloseFragment() {
        activityNavigator.onBack()
    }


    fun openMap(){
        activityNavigator.replaceFragmentInContentFrame(MapMenuFragment.TAG, BaseActivity.DEFAULT_CONTENT_VIEW_TAB, null)
        activityNavigator.replaceFragmentInContentFrame(MapFragment.TAG, BaseActivity.DEFAULT_CONTENT_VIEW_BACKGROUND, null)

    }

    fun openRoboList() {
        activityNavigator.replaceFragmentInContentFrame(RoboMenuFragment.TAG, BaseActivity.DEFAULT_CONTENT_VIEW_TAB, null)

    }


}