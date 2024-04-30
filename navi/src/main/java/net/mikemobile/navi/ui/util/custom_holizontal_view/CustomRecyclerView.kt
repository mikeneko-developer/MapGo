package net.mikemobile.navi.ui.util.custom_holizontal_view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.view.ViewTreeObserver
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import java.lang.String


fun SnapHelper.getSnapPosition(recyclerView: RecyclerView): Int {
    val layoutManager = recyclerView.layoutManager ?: return RecyclerView.NO_POSITION
    val snapView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
    return layoutManager.getPosition(snapView)
}

fun RecyclerView.attachSnapHelperWithListener(
    snapHelper: SnapHelper,
    behavior: SnapOnScrollListener.Behavior = SnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL,
    onSnapPositionChangeListener: OnSnapPositionChangeListener) {
    snapHelper.attachToRecyclerView(this)
    val snapOnScrollListener = SnapOnScrollListener(snapHelper, behavior, onSnapPositionChangeListener)
    addOnScrollListener(snapOnScrollListener)
}

class CustomRecyclerView : RecyclerView {
    var orientationType:Int = LinearLayoutManager.VERTICAL

    constructor(context: Context) : super(context) {
        setup(null)
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        setup(attrs)
    }

    private var customRecyclerViewListener: CustomRecyclerViewListener? = null
    fun setOnViewListener(l: CustomRecyclerViewListener) {
        customRecyclerViewListener = l
    }


    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    fun setup(attrs: AttributeSet?) {
        clipToPadding = false


        // 一回だけビューがレンダリングされた時呼ばれる処理
        globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            Log.i(
                "CustomRecyclerView", "OnGlobalLayoutListener#onGlobalLayout() " +
                        "Width = " + String.valueOf(this.getWidth()) + ", " +
                        "Height = " + String.valueOf(this.getHeight())
            )

            // removeOnGlobalLayoutListener()の削除
            this.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener)

            customRecyclerViewListener?.let{
                it.onRendering(this.width, this.height)
            }
        }

        this.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener)

        setScrollListener()

    }



    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        Log.i(
            "CustomRecyclerView", "OnGlobalLayoutListener#onGlobalLayout() " +
                    "left = " + String.valueOf(left) + ", " +
                    "top = " + String.valueOf(top) + ", " +
                    "right = " + String.valueOf(right) + ", " +
                    "bottom = " + String.valueOf(bottom) + ""
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

    }


    var oneItemHeight = -1
    var topPaddingHeight = -1
    fun setItemCount(count: Int){

        if(orientationType == LinearLayoutManager.VERTICAL) {

            val addressListHeight = this.getHeight()
            oneItemHeight = addressListHeight / count
            topPaddingHeight = (addressListHeight / 2) - (oneItemHeight / 2)

            setItemViewCacheSize(count * 3)
        }else {
            oneItemHeight = this.getHeight()
            topPaddingHeight = 0

            setItemViewCacheSize(count * 3)
        }
    }

    fun setItemHeight(height: Int){

        if(orientationType == LinearLayoutManager.VERTICAL) {
            val addressListHeight = this.getHeight()
            val count = addressListHeight / oneItemHeight

            oneItemHeight = height
            topPaddingHeight = (addressListHeight / 2) - (height / 2)

            setItemViewCacheSize(count * 3)
        }else {
            oneItemHeight = this.getHeight()
            topPaddingHeight = 0

            setItemViewCacheSize(5)
        }
    }


    override fun setAdapter(adapter: Adapter<*>?) {

        if (adapter is CustomBaseAdapter && oneItemHeight != -1) {
            (adapter as CustomBaseAdapter).oneItemHeight = oneItemHeight
        }

        super.setAdapter(adapter)

        if (topPaddingHeight != -1) {
            setPaddingHeight(topPaddingHeight)
        }
    }

    fun setPaddingHeight(height:Int){
        if(orientationType == LinearLayoutManager.VERTICAL) {
            setPadding(0, height, 0, height)
        }
        onRendering()
    }

    fun onRendering(){
        val snapHelperStart = LinearSnapHelper()
        attachSnapHelperWithListener(
            snapHelperStart,
            SnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL_STATE_IDLE,
            object : OnSnapPositionChangeListener {
                override fun onSnapPositionProcessing() {
                    //searchViewModel.setListScrolling(true)
                }

                override fun onSnapPositionChange(position: Int, isChanged: Boolean) {
                    //searchViewModel.setListScrolling(false)
                    if (isChanged) {
                        //searchViewModel.onSelectedIndexChanged(position)
                        customRecyclerViewListener?.onSelectPosition(position)
                    }
                }
            })
    }


    private var isScrollPositionX = 0
    private var isScrollPositionY = 0
    private var positionIndex = -1


    private fun setScrollListener(){
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val manager = recyclerView.layoutManager as LinearLayoutManager
                positionIndex = manager.findFirstVisibleItemPosition()


                isScrollPositionX += dx
                isScrollPositionY += dy

                adapter?.let{
                    if(it is CustomBaseAdapter){
                        (it as CustomBaseAdapter).scrollPositionX = -(isScrollPositionX)
                        (it as CustomBaseAdapter).scrollPositionY = -(isScrollPositionY)
                        (it as CustomBaseAdapter).selectPosition = positionIndex
                        (it as CustomBaseAdapter).notifyDataSetChanged()
                    }
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                android.util.Log.i("!!!!!","newState:" + newState)

            }
        })
    }
}