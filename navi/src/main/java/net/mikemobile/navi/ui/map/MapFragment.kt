package net.mikemobile.navi.ui.map

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import net.mikemobile.databindinglib.base.BaseFragment
import net.mikemobile.databindinglib.base.BaseNavigator

import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.SupportMapFragment
import org.koin.android.viewmodel.ext.android.viewModel
import net.mikemobile.navi.R
import net.mikemobile.navi.databinding.FragmentMapBinding
import android.view.WindowInsets
import net.mikemobile.navi.util.Constant


interface MapFragmentNavigator: BaseNavigator {
    fun onError(error:String)
    fun onToast(text: String)
}

class MapFragment: BaseFragment(),
    MapFragmentNavigator, View.OnApplyWindowInsetsListener {

    private val viewModel: MapViewModel by viewModel()
    private lateinit var binding: FragmentMapBinding

    companion object {
        const val TAG = "MapFragment"
        fun newInstance() = MapFragment()
    }

    // ---------------------------------------------------------------------------------------------
    //データバインディングを有効にする
    override fun isDataBinding(): Boolean{
        return true
    }

    //
    override fun onCreateViewBinding(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{

        binding = DataBindingUtil.inflate<FragmentMapBinding>(inflater, R.layout.fragment_map, container,false)
        val view = binding.root
        viewModel.navigator = this
        binding.viewmodel = viewModel

        binding.lifecycleOwner = this

        /////////////////////////////////////
        viewModel.initialize(requireContext())

        view.setOnApplyWindowInsetsListener(this)

        /////////////////////////////////////

        val angleMode = viewModel.angleMode.value
        if (angleMode == null) {
            viewModel.mapRepository.angleMode.value = 0
            setAngleToViewSize(0)
        } else {
            setAngleToViewSize(angleMode)
        }

        val r: Resources = this.getResources()
        var bitmap = BitmapFactory.decodeResource(r, R.drawable.current_car_icon)
        binding.characterView?.setImageBitmap(bitmap)

        /**
        // MapFragmentの生成
        val mapFragment: SupportMapFragment = SupportMapFragment.newInstance()

        // MapViewをMapFragmentに変更する
        val fragmentTransaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.mapView, mapFragment)
        fragmentTransaction.commit()

        mapFragment.getMapAsync(viewModel)
         */



        // MapFragmentの生成

        var map : SupportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        if (map == null) {
            map = SupportMapFragment.newInstance()
            childFragmentManager.beginTransaction().replace(R.id.map, map).commit()
        }
        map.getMapAsync(viewModel)


        binding.debugView?.let {
            viewModel.setDebugView(it)
            it.viewTreeObserver?.addOnGlobalLayoutListener {
                // 地図を表示するViewのサイズを取得
                //Toast.makeText(requireContext(), "サイズ取得", Toast.LENGTH_SHORT).show()
                //viewModel.setMapViewSize(view.width, view.height)
            }
        }
        binding.characterView?.let {
            viewModel.setCharacterView(it)
            it.viewTreeObserver?.addOnGlobalLayoutListener {
                // 地図を表示するViewのサイズを取得
                //Toast.makeText(requireContext(), "サイズ取得", Toast.LENGTH_SHORT).show()
                //viewModel.setMapViewSize(view.width, view.height)
            }
        }

        return view
    }


    override fun onApplyWindowInsets(p0: View?, p1: WindowInsets?): WindowInsets {
        TODO("Not yet implemented")
    }

    //
    override fun onActivityCreate(savedInstanceState: Bundle?) {
        //activityNavigator?.replaceFragmentInSecondContentFrame(MapTopFragment.TAG)

        binding.mapDefaultFrame?.let {
            it.viewTreeObserver?.addOnGlobalLayoutListener {
                // 地図を表示するViewのサイズを取得
                //Toast.makeText(requireContext(), "サイズ取得", Toast.LENGTH_SHORT).show()
                //viewModel.setMapViewSize(view.width, view.height)

                binding.mapViewFrame?.layoutParams?.height = (it.height * 1.6f).toInt()

                it.viewTreeObserver?.removeOnGlobalFocusChangeListener { oldFocus, newFocus ->  }

                val angleMode = viewModel.angleMode.value
                if (angleMode != null) {
                    setAngleToViewSize(angleMode)
                }
            }
        }


        binding.compass.setOnClickListener {

            viewModel.buttonClickCompass()

            val angleMode = viewModel.angleMode.value
            if (angleMode == null) {
                return@setOnClickListener
            }

            if (angleMode == 0) {
                setAngleToViewSize(0)
                binding.compass.setImageResource(R.drawable.icon_direction_nouth)
                binding.compass.rotationX = 0f
            } else if (angleMode == 1) {
                setAngleToViewSize(1)
                binding.compass.setImageResource(R.drawable.icon_direction)
                binding.compass.rotationX = 0f
            } else {
                setAngleToViewSize(2)
                binding.compass.setImageResource(R.drawable.icon_direction)
                binding.compass.rotationX = 45f
            }
        }


    }

    override fun onResume() {
        super.onResume()
        viewModel.resume(this)
        setObserve()
    }

    override fun onPause() {
        super.onPause()
        viewModel.pause(this)
        removeObserve()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.destroy(this)
    }

    //
    override fun onBack() {

    }

    /**
     * マップの位置調整
     */
    private fun setAngleToViewSize(angleMode: Int) {

        binding.mapViewFrame?.let {
            val y = if (angleMode != 0) {
                0f
            } else {
                -((it.height * 0.34f) / 2)
            }

            binding.mapViewFrame?.y = y
        }
    }

    private fun setObserve() {

        viewModel.realTimeAngle.observe(this) {

            val angleMode = viewModel.angleMode.value
            if (angleMode == null) {
                return@observe
            }

            if (angleMode == 0) {
                binding.characterView?.setRotate(it)
                binding.characterView?.setTilt(0f)
                return@observe
            }

            binding.characterView?.setRotate(0f)
            if (angleMode == 2) {
                binding.characterView?.setTilt(45f)
            } else {
                binding.characterView?.setTilt(0f)
            }

            if (viewModel.view_mapStatus.value == MapController.Companion.MAP_STATUS.TOUCH_MOVE.toString()
                || viewModel.view_mapStatus.value == MapController.Companion.MAP_STATUS.MAP_DIRECT_MOVE.toString()) {
                viewModel.mapCtl?.onAngle(it)
                return@observe
            }

            val location = viewModel.getCurrentLocation() ?: return@observe
            viewModel.mapCtl?.onAngle(location, it, false)

        }
    }

    private fun removeObserve() {
        viewModel.realTimeAngle.removeObservers(this)
    }

    // ---------------------------------------------------------------------------------------------
    // BaseNavigatorのメソッド
    override fun onCloseFragment() {

    }

    override fun onError(error:String){
        Toast.makeText(context,"" + error, Toast.LENGTH_SHORT).show()
    }

    override fun onToast(text: String) {
        Toast.makeText(context,"" + text, Toast.LENGTH_SHORT).show()
    }
}