package com.sp.app.mapgo.ui.map

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.google.android.gms.maps.model.LatLng
import com.sp.app.mapgo.R
import com.sp.app.mapgo.databinding.FragmentMapBinding
import com.sp.app.maplib.ui.map.MapBaseFragment


class MapFragment: MapBaseFragment() {

    private lateinit var binding: FragmentMapBinding
    companion object {
        const val TAG = "MapFragment"
        fun newInstance() = MapFragment()
    }

    // ---------------------------------------------------------------------------------------------
    // マップから取得した現在位置
    override fun onMapToCurrentPosition(latLng: LatLng, point: Point) {
        binding.debugView.setPoint(point)
        binding.characterView.setPoint(point)
        binding.directionView1.setPoint(point)
    }

    // デバイスの方向が更新された時呼び出される
    override fun onChangeDeviceOrientation(orientation: Float) {
        if (viewModel.angleMode.value == 0) {
            binding.directionView1.setRotate(orientation)
        } else {
            binding.directionView1.setRotate(0f)
        }
    }

    //
    override fun onChangeAngleMode(angleMode: Int) {
        if (angleMode == 0) {
            binding.compassImage.setImageResource(com.sp.app.maplib.R.drawable.icon_direction_nouth)
            binding.compassImage.rotationX = 0f
        } else if (angleMode == 1) {
            binding.compassImage.setImageResource(com.sp.app.maplib.R.drawable.icon_direction)
            binding.compassImage.rotationX = 0f
        } else {
            binding.compassImage.setImageResource(com.sp.app.maplib.R.drawable.icon_direction)
            binding.compassImage.rotationX = 45f
        }
    }

    // マップのViewのサイズ
    override fun onMapArea(startPoint: Point, endPoint: Point) {
        binding.debugView.setMapEnableArea(startPoint, endPoint)
        binding.characterView.setMapEnableArea(startPoint, endPoint)
        binding.directionView1.setMapEnableArea(startPoint, endPoint)
    }

    override fun onChangeAngle(
        angleMode: Int,
        deviceOrientation: Float,
        tilt: Float
    ) {
        if (angleMode == 0) {
            binding.directionView1.setRotate(deviceOrientation)
            binding.directionView1.setTilt(tilt)

            binding.characterView.setRotate(deviceOrientation)
            binding.characterView.setTilt(tilt)
            return
        }

        binding.characterView.setRotate(0f)
        binding.directionView1.setRotate(deviceOrientation)

        if (angleMode == 2) {
            binding.characterView.setTilt(tilt)
            binding.directionView1.setTilt(tilt)
        } else {
            binding.characterView.setTilt(tilt)
            binding.directionView1.setTilt(tilt)
        }
    }

    override fun getMainFrame(): View? {
        return binding.topView
    }

    override fun getMapFrame(): View? {
        return binding.mapViewFrame
    }

    override fun getMapResourceId(): Int {
        return R.id.map
    }

    //
    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container,false)
        val view = binding.root
        binding.viewmodel = viewModel
        binding.lifecycleOwner = this

        viewModel.mapRepository.angleMode.value = 1

        super.onCreateViewBindingSetup(inflater, container, savedInstanceState)

        return view
    }

    //
    override fun onActivityCreate(savedInstanceState: Bundle?) {
        binding.compassImage.setOnClickListener {

            val angleMode = viewModel.angleMode.value
            if (angleMode == 2) {
                viewModel.setCompassMode(1)
            } else {
                viewModel.setCompassMode(2)
            }
        }
        binding.zoomUp.setOnClickListener {
            viewModel.mapCtl?.onZoomIn()
        }
        binding.zoomDown.setOnClickListener {
            viewModel.mapCtl?.onZoomOut()
        }
        binding.location.setOnClickListener {
            val location = viewModel.getCurrentLocation() ?: return@setOnClickListener
            viewModel.mapCtl?.onFocusCurrent(location)
        }

        val r: Resources = this.getResources()
        BitmapFactory.decodeResource(r, com.sp.app.maplib.R.drawable.current_car_icon)?.let {
            binding.characterView.setImageBitmap(it)
        }
        BitmapFactory.decodeResource(r, R.drawable.arrow_orientation)?.let {
            binding.directionView1.setImageBitmap(it)
        }
    }

    override fun onResume() {
        super.onResume()
        setObserve()
    }

    override fun onPause() {
        super.onPause()
        removeObserve()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    //
    override fun onBack() { }

    ////////////////////////////////////////////////////////////////////////
    private fun setObserve() {


    }

    private fun removeObserve() {

    }

    ////////////////////////////////////////////////////////////////////////



}