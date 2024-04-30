package net.mikemobile.navi.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import net.mikemobile.databindinglib.base.BaseFragment
import net.mikemobile.databindinglib.base.BaseNavigator
import net.mikemobile.navi.R
import net.mikemobile.navi.databinding.FragmentRoboMenuBinding
import net.mikemobile.navi.ui.robos.RoboListFragment
import net.mikemobile.navi.ui.robos.TalkFragment
import org.koin.android.viewmodel.ext.android.viewModel

interface RoboMenuFragmentNavigator: BaseNavigator {
    fun onClickMenu()
    fun onClickRobo()
    fun onClickTalk()
    fun onClickImage()
}


class RoboMenuFragment : BaseFragment(),
    RoboMenuFragmentNavigator {

    companion object {
        const val TAG = "RoboMenuFragment"
        fun newInstance() = RoboMenuFragment()
    }


    private val viewModel: RoboMenuFragmentViewModel by viewModel()

    // ---------------------------------------------------------------------------------------------
    //データバインディングを有効にする
    override fun isDataBinding(): Boolean{
        return true
    }

    //
    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{

        val binding = DataBindingUtil.inflate<FragmentRoboMenuBinding>(inflater, R.layout.fragment_robo_menu, container,false)
        val view = binding.root

        binding.viewmodel = viewModel

        viewModel.initialize()
        viewModel.navigator = this


        return view
    }

    //
    override fun onActivityCreate(savedInstanceState: Bundle?) {

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

    override fun onBack() {
        activityNavigator.onBack()
    }


    override fun onClickRobo() {
        activityNavigator.replaceFragmentToBackStackInMainContentFrame(RoboListFragment.TAG)
    }

    override fun onClickTalk() {
        activityNavigator.replaceFragmentToBackStackInMainContentFrame(TalkFragment.TAG)

    }

    override fun onClickImage() {

    }

    override fun onClickMenu() {
        activityNavigator.replaceFragmentToBackStackInPopupContentFrame(MenuFragment.TAG)
    }

    /**
    override fun onBack() {

    }
     */

    // ---------------------------------------------------------------------------------------------
    // BaseNavigatorのメソッド
    override fun onCloseFragment() {

    }



}