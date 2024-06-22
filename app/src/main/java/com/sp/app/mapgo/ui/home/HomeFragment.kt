package com.sp.app.mapgo.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.sp.app.mapgo.R
import com.sp.app.mapgo.databinding.FragmentGameBinding
import com.sp.app.mapgo.databinding.FragmentHomeBinding
import com.sp.app.mapgo.ui.viewmodel.GameViewModel
import com.sp.app.mapgo.ui.viewmodel.HomeViewModel
import com.sp.app.maplib.data.MapLocation
import com.sp.app.maplib.listener.OnMapListener
import com.sp.app.maplib.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomeFragment : BaseFragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModel()

    companion object {
        const val TAG = "HomeFragment"
        fun newInstance() = HomeFragment()
    }

    // ---------------------------------------------------------------------------------------------


    //
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
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