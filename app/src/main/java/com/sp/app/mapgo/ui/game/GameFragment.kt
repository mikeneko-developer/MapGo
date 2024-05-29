package com.sp.app.mapgo.ui.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.sp.app.mapgo.R
import com.sp.app.mapgo.databinding.FragmentGameBinding
import com.sp.app.mapgo.ui.viewmodel.GameViewModel
import com.sp.app.maplib.data.MapLocation
import com.sp.app.maplib.listener.OnMapListener
import com.sp.app.maplib.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel


class GameFragment : BaseFragment(), OnMapListener {

    private lateinit var binding: FragmentGameBinding
    private val viewModel: GameViewModel by viewModel()

    companion object {
        const val TAG = "GameFragment"
        fun newInstance() = GameFragment()
    }

    // ---------------------------------------------------------------------------------------------


    //
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_game, container, false)
        val view = binding.root
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        /////////////////////////////////////
        viewModel.initialize()
        return view
    }

    //
    override fun onActivityCreate(savedInstanceState: Bundle?) {
        viewModel.create()
    }

    override fun onResume() {
        super.onResume()
        viewModel.resume()
        setObserve()
    }

    override fun onPause() {
        super.onPause()
        viewModel.pause()
        removeObserve()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.destroy()
    }

    //
    override fun onBack() {}

    ////////////////////////////////////////////////////////////////////////
    private fun setObserve() {


    }

    private fun removeObserve() {

    }

    ////////////////////////////////////////////////////////////////////////
    override fun onSelectMapPoint(pointData: MapLocation) {

    }

    override fun onAddMarker(pointData: MapLocation) {

    }

    override fun onChangeMarker(pointData: MapLocation) {

    }

    override fun onRemoveMarker(pointData: MapLocation) {

    }

    override fun onAddFavoriteMarker(pointData: MapLocation) {

    }

}