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
import net.mikemobile.navi.databinding.DialogListBinding
import net.mikemobile.navi.databinding.DialogSelectButtonBinding
import org.koin.android.viewmodel.ext.android.viewModel

interface ListDialogNavigator: BaseNavigator {
    fun onSelect(position: Int)
    fun onCancel()
}


class ListDialog : BaseDialog(), ListDialogNavigator {

    companion object {
        const val TAG = "ListDialog"
        fun newInstance() = ListDialog()
    }


    private val viewModel: ListDialogViewModel by viewModel()

    override fun onBindingEnable(): Boolean {
        return true
    }

    override fun onDataBindingView(): View? {
        val binding = DataBindingUtil.inflate<DialogListBinding>(LayoutInflater.from(activity), R.layout.dialog_list, null,false)
        val view = binding.root

        binding.viewmodel = viewModel
        viewModel.navigator = this

        viewModel.setRecyclerView(context!!, binding.recyclerview)

        return view
    }

    override fun onCreateDialogView(savedInstanceState: Bundle?, dialog: Dialog) {

    }

    override fun onCancelDialog() {
        onCancel()
    }

    override fun onSelect(position: Int) {
        val data = Intent()
        data.putExtra("id", 1)
        data.putExtra("select", position)
        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)

    }

    override fun onCancel() {
        val data = Intent()
        data.putExtra("id", 4)
        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, data)
    }

    override fun onCloseFragment() {
        this.close()
    }

}