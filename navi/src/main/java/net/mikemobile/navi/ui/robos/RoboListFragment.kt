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
import org.koin.android.viewmodel.ext.android.viewModel
import net.mikemobile.navi.R
import net.mikemobile.navi.databinding.FragmentMapBinding
import net.mikemobile.navi.databinding.FragmentMapRouteBinding
import net.mikemobile.navi.databinding.FragmentMapTopBinding
import net.mikemobile.navi.databinding.FragmentRoboListBinding
import net.mikemobile.navi.ui.dialog.ListDialog
import net.mikemobile.navi.ui.dialog.SelectButtonDialog
import net.mikemobile.navi.util.Constant

interface RoboListFragmentNavigator: BaseNavigator {
    fun onError(error:String)
    fun onClickOpenDialog()
}

class RoboListFragment: BaseFragment(),
    RoboListFragmentNavigator {

    private val viewModel: RoboListViewModel by viewModel()

    companion object {
        const val TAG = "RoboListFragment"
        fun newInstance() = RoboListFragment()
    }

    // ---------------------------------------------------------------------------------------------
    //データバインディングを有効にする
    override fun isDataBinding(): Boolean{
        return true
    }

    //
    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{

        val binding = DataBindingUtil.inflate<FragmentRoboListBinding>(inflater, R.layout.fragment_robo_list, container,false)
        val view = binding.root
        viewModel.navigator = this
        binding.viewmodel = viewModel

        binding.lifecycleOwner = this

        /////////////////////////////////////

        viewModel.setRecyclerView(requireContext(), binding.recyclerview)
        viewModel.initialize()


        return view
    }

    //
    override fun onActivityCreate(savedInstanceState: Bundle?) {

    }

    override fun onResume() {
        super.onResume()
        viewModel.resume(this)
        viewModel.initializeList()
    }

    override fun onPause() {
        super.onPause()
        viewModel.pause(this)
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
        if (requestCode == Constant.Companion.DIALOG_ID.SELECT_POINT_MENU.id && resultCode == Activity.RESULT_OK) {
            data?.let {
                var id = it.getIntExtra("id",-1)

                if(id == 1) {
                    var position = it.getIntExtra("select" , -1)
                    viewModel.selectItemPosition(position)
                }
            }
        }else if (requestCode == Constant.Companion.DIALOG_ID.MAIN_MENU.id && resultCode == Activity.RESULT_OK) {

        }
    }

    // ---------------------------------------------------------------------------------------------
    // BaseNavigatorのメソッド
    override fun onCloseFragment() {

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