package com.sp.app.mapgo.ui.map

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.databinding.DataBindingUtil

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.sp.app.mapgo.R
import com.sp.app.mapgo.databinding.FragmentMapBinding
import com.sp.app.maplib.Constant
import com.sp.app.maplib.MapController
import com.sp.app.maplib.OnMapControllerListener
import com.sp.app.maplib.data.MapLocation
import com.sp.app.mapgo.ui.viewmodel.MapCtlViewModel
import com.sp.app.maplib.ui.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel


class MapFragment: BaseFragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentMapBinding
    private val viewModel: MapCtlViewModel by viewModel()
    companion object {
        const val TAG = "MapFragment"
        fun newInstance() = MapFragment()
    }

    // ---------------------------------------------------------------------------------------------

    private val mapControllerListener = object: OnMapControllerListener {
        override fun onStatus(status: MapController.Companion.MAP_STATUS) {
            viewModel.mapRepository.mapStatusString.postValue("" + status)
        }
        override fun onRequestCurrent() {
            val location = viewModel.getCurrentLocation() ?: return
            viewModel.mapCtl?.onCurrent(location)
        }

        override fun onChangeAngle(angle: Float) {
            viewModel.onChangeAngle(angle)
        }

        override fun onChangeZoom(zoom: Float) {
            viewModel.onChangeZoom(zoom)
        }
        override fun onLastCurrent(latLng: LatLng, point: Point) {
            binding.debugView.setPoint(point)
            binding.characterView.setPoint(point)
            binding.directionView1.setPoint(point)
            binding.directionView2.setPoint(point)
        }

        override fun onLongClickMap(point: MapLocation) {
            viewModel.onLongClickMap(point)
        }
        override fun onPoiClickMap(point: MapLocation) {}
        override fun onMoveStartMap(isTouch: Boolean) {}
        override fun onMoveMap() {}
        override fun onMoveEndMap(isTouch: Boolean) {}
        override fun onMarkerClickMap(
            marker_type: MapController.Companion.MARKER_TYPE,
            marker_id: String
        ) {}
    }

    //
    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container,false)
        val view = binding.root
        binding.viewmodel = viewModel

        binding.lifecycleOwner = this

        // マップのサイズをViewのサイズに調整する処理
        viewSizeSetup(binding.topView)

        val data = Constant.getDisplaySize(requireActivity())
        binding.mapViewFrame.layoutParams.height = (data.y * 1.4f).toInt()

        val angleMode = viewModel.angleMode.value
        if (angleMode == null) {
            viewModel.mapRepository.angleMode.value = 0
            setAngleToViewSize(0)
        } else {
            setAngleToViewSize(angleMode)
        }

        /////////////////////////////////////
        viewModel.initialize()

        /////////////////////////////////////


        // MapFragmentの生成と設定
        var map : SupportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        if (map == null) {
            map = SupportMapFragment.newInstance()
            childFragmentManager.beginTransaction().replace(R.id.map, map).commit()
        }
        map.getMapAsync(this)

        return view
    }

    //
    override fun onActivityCreate(savedInstanceState: Bundle?) {
        binding.compassImage.setOnClickListener {
            viewModel.changeCompassMode()
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
        BitmapFactory.decodeResource(r, R.drawable.arrow_direction)?.let {
            binding.directionView2.setImageBitmap(it)
        }

        viewModel.create()
    }

    override fun onResume() {
        super.onResume()
        viewModel.resume(requireContext())
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
    override fun onBack() { }

    ////////////////////////////////////////////////////////////////////////
    private fun setObserve() {

        viewModel.deviceOrientation.observe(this) {
            binding.directionView1.setRotate(it)
            moveAngle(it, viewModel.currentDirection.value!!)
        }

        viewModel.currentDirection.observe(this) {
            binding.directionView2.setRotate(it)
        }

        viewModel.angleMode.observe(this) {angleMode ->
            setAngleToViewSize(angleMode)
        }

    }

    private fun removeObserve() {
        viewModel.currentLocation.removeObservers(this)
        viewModel.deviceOrientation.removeObservers(this)
        viewModel.angleMode.removeObservers(this)
    }
    ////////////////////////////////////////////////////////////////////////
    /**
     * マップの位置調整
     */
    private fun setAngleToViewSize(angleMode: Int) {

        val data = Constant.getDisplaySize(requireActivity())
        val y = if (angleMode != 0) {
            0f
        } else {
            -((data.y * 0.4f) / 2)
        }

        binding.mapViewFrame.y = y

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

    private fun viewSizeSetup(mainView: View) {
        mainView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                mainView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                Log.i(TAG, "addOnGlobalLayoutListener")
                // 地図を表示するViewのサイズを一回だけ取得
                val width = mainView.width
                val height = mainView.height

                viewModel.setViewSize(width, height)
            }
        })
    }

    // MapFragmentの初期化関連
    var centerMoveX = 0.0
    var centerMoveY = 0.0
    private fun setMapViewSize(width: Int, height: Int) {

        val startPoint = Point(0,height/10)
        val endPoint = Point(width,height - (height/ 5 * 2))
        binding.debugView.setMapEnableArea(startPoint, endPoint)
        binding.characterView.setMapEnableArea(startPoint, endPoint)
        binding.directionView1.setMapEnableArea(startPoint, endPoint)
        binding.directionView2.setMapEnableArea(startPoint, endPoint)

        val areaWidth = endPoint.x - startPoint.x
        val areaHeight = endPoint.y - startPoint.y

        viewModel.mapCtl?.onDisplayArea(areaWidth, areaHeight / 3 * 2)

        centerMoveX = 0.0
        centerMoveY = (areaHeight/2 + startPoint.y/2).toDouble()

        //centerMoveY = 700.0

        viewModel.mapCtl?.setMoveCenter(centerMoveX, centerMoveY)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        context?.let {
            viewModel.mapCtl = MapController(it, googleMap, mapControllerListener)

            val width = viewModel.getMapWidth()
            val height = viewModel.getMapHeight()

            setMapViewSize(width, height)
        }
    }

    ////////////////////////////////////////////////////////////////////////
    private fun moveAngle(deviceOrientation: Float, direction: Float) {
        val angleMode = viewModel.angleMode.value ?: return

        // 角度の判定
        var _angle = deviceOrientation

        // 現在移動と判定されるかどうか
        var moving = false

        if (direction > 0) {
            //moving = true
            _angle = direction
        }

        _angle = deviceOrientation

        binding.debugText.text = viewModel.getDebugText()

        if (angleMode == 0) {
            binding.characterView.setRotate(_angle)
            binding.characterView.setTilt(0f)
            return
        }

        binding.characterView.setRotate(0f)
        if (angleMode == 2) {
            binding.characterView.setTilt(45f)
        } else {
            binding.characterView.setTilt(0f)
        }

        if (viewModel.view_mapStatus.value == MapController.Companion.MAP_STATUS.TOUCH_MOVE.toString()
            || viewModel.view_mapStatus.value == MapController.Companion.MAP_STATUS.MAP_DIRECT_MOVE.toString()) {
            viewModel.mapCtl?.onAngle(_angle)
            return
        }

        val location = viewModel.getCurrentLocation() ?: return
        viewModel.mapCtl?.onAngle(location, _angle, moving)
    }
}