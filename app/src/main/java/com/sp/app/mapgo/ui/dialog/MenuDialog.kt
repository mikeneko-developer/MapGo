package com.sp.app.mapgo.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import com.sp.app.mapgo.R
import com.sp.app.mapgo.databinding.DialogMenuBinding
import com.sp.app.mapgo.ui.viewmodel.GameViewModel
import com.sp.app.maplib.ui.base.BaseDialog
import org.koin.androidx.viewmodel.ext.android.viewModel


class MenuDialog : BaseDialog() {

    companion object {
        const val TAG = "MenuDialog"
        fun newInstance() = MenuDialog()
    }


    private val viewModel: GameViewModel by viewModel()

    override fun onBindingEnable(): Boolean {
        return true
    }

    override fun onDataBindingView(): View? {
        val binding = DataBindingUtil.inflate<DialogMenuBinding>(LayoutInflater.from(activity), R.layout.dialog_menu, null,false)
        val view = binding.root

        binding.viewmodel = viewModel

        return view
    }

    override fun onCreateDialogView(savedInstanceState: Bundle?, dialog: Dialog) {

    }

    override fun onCancelDialog() {

    }

}