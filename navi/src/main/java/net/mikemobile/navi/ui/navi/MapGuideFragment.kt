package net.mikemobile.navi.ui.navi

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
import org.koin.android.viewmodel.ext.android.viewModel
import net.mikemobile.navi.R
import net.mikemobile.navi.databinding.FragmentMapBinding
import net.mikemobile.navi.databinding.FragmentMapGuideBinding
import net.mikemobile.navi.databinding.FragmentMapRouteBinding
import net.mikemobile.navi.databinding.FragmentMapTopBinding
import net.mikemobile.navi.ui.dialog.SelectButtonDialog

interface MapGuideFragmentNavigator: BaseNavigator {
    fun onError(error:String)
    fun onFinishGuide()
}

class MapGuideFragment: BaseFragment(),
    MapGuideFragmentNavigator {

    private val viewModel: MapGuideViewModel by viewModel()

    companion object {
        const val TAG = "MapGuideFragment"
        fun newInstance() = MapGuideFragment()
    }

    // ---------------------------------------------------------------------------------------------
    //データバインディングを有効にする
    override fun isDataBinding(): Boolean{
        return true
    }

    //
    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{

        val binding = DataBindingUtil.inflate<FragmentMapGuideBinding>(inflater, R.layout.fragment_map_guide, container,false)
        val view = binding.root
        viewModel.navigator = this
        binding.viewmodel = viewModel

        binding.lifecycleOwner = this

        /////////////////////////////////////

        viewModel.setRecyclerView(binding.horizontalList)
        viewModel.initialize()


        return view
    }

    //
    override fun onActivityCreate(savedInstanceState: Bundle?) {
        viewModel.start(this)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.end(this)
    }

    override fun onBack() {

    }

    // ---------------------------------------------------------------------------------------------
    // BaseNavigatorのメソッド
    override fun onCloseFragment() {

    }

    override fun onError(error:String){
        Toast.makeText(context,"" + error, Toast.LENGTH_SHORT).show()
    }

    override fun onFinishGuide() {
        activityNavigator.replaceFragmentInMainContentFrame(MapTopFragment.TAG)
    }

    // ----------------

    fun openDialog() {
        activityNavigator.showDialogFragmentWithTargetFragment(SelectButtonDialog.TAG, this, 1, Bundle())
    }
}