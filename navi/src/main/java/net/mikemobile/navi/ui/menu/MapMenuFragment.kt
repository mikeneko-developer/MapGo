package net.mikemobile.navi.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import net.mikemobile.databindinglib.base.BaseFragment
import net.mikemobile.databindinglib.base.BaseNavigator
import net.mikemobile.navi.R
import net.mikemobile.navi.databinding.FragmentMapMenuBinding
import net.mikemobile.navi.system.startPictureInPicture
import net.mikemobile.navi.ui.navi.favorite.FavoriteListFragment
import org.koin.android.viewmodel.ext.android.viewModel

interface MapMenuFragmentNavigator: BaseNavigator {
    fun onMenuSelect(tag: String)
    fun onClickFavorite()
    fun onClickHome()
    fun onClickMenu()
    fun onClickPictureInPicture()
}


class MapMenuFragment : BaseFragment(),
    MapMenuFragmentNavigator {

    companion object {
        const val TAG = "MapMenuFragment"
        fun newInstance() = MapMenuFragment()
    }


    private val viewModel: MapMenuFragmentViewModel by viewModel()

    // ---------------------------------------------------------------------------------------------
    //データバインディングを有効にする
    override fun isDataBinding(): Boolean{
        return true
    }

    //
    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{

        val binding = DataBindingUtil.inflate<FragmentMapMenuBinding>(inflater, R.layout.fragment_map_menu, container,false)
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

    override fun onBack() {
        activityNavigator.onBack()
    }

    override fun onMenuSelect(tag: String) {

    }

    override fun onClickFavorite() {
        activityNavigator.replaceFragmentToBackStackInSecondContentFrame(FavoriteListFragment.TAG)
    }

    override fun onClickHome() {

    }

    override fun onClickMenu() {
        activityNavigator.replaceFragmentToBackStackInPopupContentFrame(MenuFragment.TAG)
    }

    override fun onClickPictureInPicture() {
        activityNavigator.startPictureInPicture()
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