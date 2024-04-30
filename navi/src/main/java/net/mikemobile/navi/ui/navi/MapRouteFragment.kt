package net.mikemobile.navi.ui.navi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import net.mikemobile.databindinglib.base.BaseFragment
import net.mikemobile.databindinglib.base.BaseNavigator

import androidx.databinding.DataBindingUtil
import org.koin.android.viewmodel.ext.android.viewModel
import net.mikemobile.navi.R
import net.mikemobile.navi.databinding.FragmentMapBinding
import net.mikemobile.navi.databinding.FragmentMapRouteBinding
import net.mikemobile.navi.databinding.FragmentMapTopBinding
import net.mikemobile.navi.ui.dialog.SelectButtonDialog

interface MapRouteFragmentNavigator: BaseNavigator {
    fun onError(error:String)
    fun onClickGuideStart()
}

class MapRouteFragment() : BaseFragment(),
    MapRouteFragmentNavigator {

    private val viewModel: MapRouteViewModel by viewModel()

    companion object {
        const val TAG = "MapRouteFragment"
        fun newInstance() = MapRouteFragment()
    }

    // ---------------------------------------------------------------------------------------------
    //データバインディングを有効にする
    override fun isDataBinding(): Boolean{
        return true
    }

    //
    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{

        val binding = DataBindingUtil.inflate<FragmentMapRouteBinding>(inflater, R.layout.fragment_map_route, container,false)
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

    }

    override fun onResume() {
        super.onResume()
        viewModel.resume(this)

        requireActivity().onBackPressedDispatcher.addCallback(object:
            OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                Toast.makeText(context,"handleOnBackPressed",Toast.LENGTH_SHORT).show()
            }
        })



    }

    override fun onPause() {
        super.onPause()
        viewModel.pause(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.destroy()
    }
    override fun onBack() {
        Toast.makeText(context,"onBack",Toast.LENGTH_SHORT).show()
        viewModel.onBack()
        activityNavigator.onBack()
    }

    // ---------------------------------------------------------------------------------------------
    // BaseNavigatorのメソッド
    override fun onCloseFragment() {

    }

    override fun onError(error:String){
        Toast.makeText(context,"" + error, Toast.LENGTH_SHORT).show()
    }

    override fun onClickGuideStart() {
        activityNavigator.replaceFragmentInMainContentFrame(MapGuideFragment.TAG)
    }

    // ----------------

    fun openDialog() {
        activityNavigator.showDialogFragmentWithTargetFragment(SelectButtonDialog.TAG, this, 1, Bundle())
    }
}