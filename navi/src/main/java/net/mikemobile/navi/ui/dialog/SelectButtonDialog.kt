package net.mikemobile.navi.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import net.mikemobile.databindinglib.base.BaseNavigator
import net.mikemobile.databindinglib.base_dialog.BaseDialog
import net.mikemobile.navi.R
import net.mikemobile.navi.data.map.MapLocation
import net.mikemobile.navi.databinding.DialogSelectButtonBinding
import org.koin.android.viewmodel.ext.android.viewModel

interface SelectButtonDialogNavigator: BaseNavigator {
    fun onRoute(pointData: MapLocation, )
    fun onViaRoute(pointData: MapLocation)
    fun onSavePoint(pointData: MapLocation)
    fun onClearPoint(pointData: MapLocation)
    fun onCancel()
}


class SelectButtonDialog : BaseDialog(), SelectButtonDialogNavigator {

    companion object {
        const val TAG = "SelectButtonDialog"
        fun newInstance() = SelectButtonDialog()

        const val RESULT_ID_ROUTE = 1
        const val RESULT_ID_VIA_ROUTE = 5
        const val RESULT_ID_FAVORITE = 2
        const val RESULT_ID_CREAR_POINT = 4
        const val RESULT_ID_CANCEL = 3
    }


    private val viewModel: SelectButtonDialogViewModel by viewModel()

    override fun onBindingEnable(): Boolean {
        return true
    }

    override fun onDataBindingView(): View? {
        var inflater = requireActivity().getLayoutInflater()

        val binding = DataBindingUtil.inflate<DialogSelectButtonBinding>(LayoutInflater.from(activity), R.layout.dialog_select_button, null,false)
        val view = binding.root

        binding.viewmodel = viewModel
        viewModel.navigator = this
        binding.lifecycleOwner = this

        return view
    }

    override fun onCreateDialogView(savedInstanceState: Bundle?, dialog: Dialog) {
        arguments?.getSerializable("PointData")?.let{
            viewModel.setPoint(it as MapLocation)
        }
        var haveGoal = false
        arguments?.getBoolean("haveGoal")?.let{
            viewModel.setHaveGoal(it)
        }
    }

    override fun onCancelDialog() {
        onCancel()
    }

    override fun onRoute(pointData: MapLocation) {
        val data = Intent()
        data.putExtra("id", RESULT_ID_ROUTE)
        data.putExtra("PointData", pointData)
        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
    }

    override fun onViaRoute(pointData: MapLocation) {
        val data = Intent()
        data.putExtra("id", RESULT_ID_VIA_ROUTE)
        data.putExtra("PointData", pointData)
        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
    }

    override fun onClearPoint(pointData: MapLocation) {
        val data = Intent()
        data.putExtra("id", RESULT_ID_CREAR_POINT)
        data.putExtra("PointData", pointData)
        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
    }

    override fun onSavePoint(pointData: MapLocation) {
        val data = Intent()
        data.putExtra("id", RESULT_ID_FAVORITE)
        data.putExtra("PointData", pointData)
        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
    }

    override fun onCancel() {
        val data = Intent()
        data.putExtra("id", RESULT_ID_CANCEL)
        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
    }

    override fun onCloseFragment() {
        this.close()
    }

}