package com.sp.app.mapgo.ui.goal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.sp.app.mapgo.R
import com.sp.app.mapgo.databinding.FragmentGoalBinding
import com.sp.app.mapgo.ui.viewmodel.GoalViewModel
import com.sp.app.maplib.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel


class GoalFragment : BaseFragment() {

    private lateinit var binding: FragmentGoalBinding
    private val viewModel: GoalViewModel by viewModel()

    companion object {
        const val TAG = "GoalFragment"
        fun newInstance() = GoalFragment()
    }

    // ---------------------------------------------------------------------------------------------


    //
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_goal, container, false)
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