package net.mikemobile.navi.ui.util

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import java.lang.String

class DebugLineView : View {

    constructor(context: Context) : super(context) {
        setup(null)
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setup(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        setup(attrs)
    }

    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    fun setup(attrs: AttributeSet?) {

        // 一回だけビューがレンダリングされた時呼ばれる処理
        globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            Log.i("DebugLineView", "OnGlobalLayoutListener#onGlobalLayout() " +
                        "Width = " + String.valueOf(this.getWidth()) + ", " +
                        "Height = " + String.valueOf(this.getHeight())
            )
            // removeOnGlobalLayoutListener()の削除
            this.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener)
        }

        this.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener)

    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        Log.i(
            "DebugLineView", "OnGlobalLayoutListener#onGlobalLayout() " +
                    "left = " + String.valueOf(left) + ", " +
                    "top = " + String.valueOf(top) + ", " +
                    "right = " + String.valueOf(right) + ", " +
                    "bottom = " + String.valueOf(bottom) + ""
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }

    private var isDebug = true
    private var point = Point(-1,-1)
    fun setPoint(point: Point) {
        this.point = point
        this.invalidate()
    }

    private var startPoint = Point(-1,-1)
    private var endPoint = Point(-1,-1)
    fun setMapEnableArea(startPoint: Point, endPoint: Point) {
        this.startPoint = startPoint
        this.endPoint = endPoint
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.TRANSPARENT)

        if (isDebug) {

            val paint = Paint()
            paint.color = Color.RED
            // 中心位置の描画
            if (point.x == -1 && point.y == -1) {
                canvas.drawLine(width / 2f, 0f, width / 2f, height.toFloat(), paint)
                canvas.drawLine(0f, height / 2f, width.toFloat(), height / 2f, paint)
            } else {
                canvas.drawLine(point.x / 2f, 0f, point.x / 2f, height.toFloat(), paint)
                canvas.drawLine(0f, point.y / 2f, width.toFloat(), point.y / 2f, paint)
            }

            val paint2 = Paint()
            paint2.color = Color.argb(100, 0,0,0)
            if (startPoint.x == -1 && startPoint.y == -1 && endPoint.x == -1 && endPoint.y == -1) {

            } else {
                canvas.drawRect(Rect(0,0,endPoint.x, startPoint.y), paint2)
                canvas.drawRect(Rect(0,endPoint.y,endPoint.x, height), paint2)

            }

        }

    }
}