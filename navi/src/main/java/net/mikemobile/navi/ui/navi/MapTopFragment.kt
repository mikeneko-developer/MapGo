package net.mikemobile.navi.ui.navi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import net.mikemobile.databindinglib.base.BaseFragment
import net.mikemobile.databindinglib.base.BaseNavigator

import androidx.databinding.DataBindingUtil
import org.koin.android.viewmodel.ext.android.viewModel
import net.mikemobile.navi.R
import net.mikemobile.navi.data.map.MapLocation
import net.mikemobile.navi.databinding.FragmentMapTopBinding
import net.mikemobile.navi.ui.dialog.SelectButtonDialog
import net.mikemobile.navi.ui.navi.favorite.FavoriteListFragment
import net.mikemobile.navi.util.Constant

interface MapTopFragmentNavigator: BaseNavigator {
    fun onError(error:String)
    fun openDialogSelectPoint(pointData: MapLocation, have_goal: Boolean)
    fun onSelectRouteMap()
    fun openMenu()
}

class MapTopFragment: BaseFragment(),
    MapTopFragmentNavigator {

    private val viewModel: MapTopViewModel by viewModel()

    companion object {
        const val TAG = "MapTopFragment"

        fun newInstance() = MapTopFragment()
    }

    // ---------------------------------------------------------------------------------------------
    //データバインディングを有効にする
    override fun isDataBinding(): Boolean{
        return true
    }

    //
    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{

        val binding = DataBindingUtil.inflate<FragmentMapTopBinding>(inflater, R.layout.fragment_map_top, container,false)
        val view = binding.root
        viewModel.navigator = this
        binding.viewmodel = viewModel

        binding.lifecycleOwner = this

        /////////////////////////////////////
        viewModel.setRecyclerView(binding.fragmentMapTopListview!!, context)

        // ドラッグアンドドロップ用設定
        //binding.fragmentMapTopListview.setHasFixedSize(true)
        viewModel.mIth.attachToRecyclerView(binding.fragmentMapTopListview)

        viewModel.initialize(this)


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
        viewModel.pause(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.destroy()
    }

    //
    override fun onBack() {
        activityNavigator.onBack()
    }

    //
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG + " ADD_MAKER","onActivityResult")
        if (requestCode == Constant.Companion.DIALOG_ID.SELECT_POINT_MENU.id && resultCode == Activity.RESULT_OK) {
            data?.let {
                var id = it.getIntExtra("id",-1)
                if (id !=  SelectButtonDialog.RESULT_ID_CANCEL) {
                    var pointData = it.getSerializableExtra("PointData") as MapLocation
                    dialogSelect(id, pointData)
                }
            }
        }else if (requestCode == Constant.Companion.DIALOG_ID.MAIN_MENU.id && resultCode == Activity.RESULT_OK) {

        }else if (requestCode == Constant.Companion.DIALOG_ID.LIST_DIALOG.id && resultCode == Activity.RESULT_OK) {
            data?.let {
                var id = it.getIntExtra("id",-1)

            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // BaseNavigatorのメソッド
    override fun onCloseFragment() {

    }

    override fun onError(error:String){
        Toast.makeText(context,"" + error, Toast.LENGTH_SHORT).show()
    }

    override fun openDialogSelectPoint(pointData: MapLocation, have_goal: Boolean){
        val bundle = Bundle()
        bundle.putBoolean("haveGoal", have_goal)
        bundle.putSerializable("PointData", pointData)

        activityNavigator.showDialogFragmentWithTargetFragment(
            SelectButtonDialog.TAG,
            this,
            Constant.Companion.DIALOG_ID.SELECT_POINT_MENU.id,
            bundle
        )
    }

    override fun onSelectRouteMap(){
        activityNavigator.replaceFragmentToBackStackInMainContentFrame(MapRouteFragment.TAG)
    }


    override fun openMenu() {
        //openListDialog()
        activityNavigator.replaceFragmentToBackStackInSecondContentFrame(FavoriteListFragment.TAG)
    }

    // ----------------

    fun openDialog() {
        activityNavigator.showDialogFragmentWithTargetFragment(SelectButtonDialog.TAG, this,
            Constant.Companion.DIALOG_ID.SELECT_POINT_MENU.id, Bundle())
    }

    // ----------------
    private fun dialogSelect(id: Int, pointData: MapLocation) {
        Log.i(TAG + " ADD_MAKER","dialogSelect")

        if(id == SelectButtonDialog.RESULT_ID_ROUTE) {
            viewModel.selectMaker(pointData)
        }else if(id == SelectButtonDialog.RESULT_ID_VIA_ROUTE) {
            viewModel.selectMaker(pointData)
        }else if(id == SelectButtonDialog.RESULT_ID_FAVORITE) {
            viewModel.setAddFavorite(pointData)
        }else if(id == SelectButtonDialog.RESULT_ID_CREAR_POINT) {
            viewModel.setClearPoint(pointData)
        }
    }

}