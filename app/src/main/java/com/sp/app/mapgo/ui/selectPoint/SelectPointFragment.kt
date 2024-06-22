package com.sp.app.mapgo.ui.selectPoint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.sp.app.mapgo.R
import com.sp.app.mapgo.databinding.FragmentSelectPointBinding
import com.sp.app.mapgo.ui.viewmodel.SelectPointViewModel
import com.sp.app.maplib.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel


class SelectPointFragment : BaseFragment() {

    private lateinit var binding: FragmentSelectPointBinding
    private val viewModel: SelectPointViewModel by viewModel()

    companion object {
        const val TAG = "SelectPointFragment"
        fun newInstance() = SelectPointFragment()
    }

    // ---------------------------------------------------------------------------------------------


    //
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_point, container, false)
        val view = binding.root
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        return view
    }

    override fun onActivityCreate(savedInstanceState: Bundle?) {

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }


}