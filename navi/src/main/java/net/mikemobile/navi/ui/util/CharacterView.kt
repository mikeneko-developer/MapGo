package net.mikemobile.navi.ui.util

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import java.lang.String

class CharacterView : View {

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
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
    }


    private var bitmap: Bitmap? = null
    fun setImageBitmap(bitmap: Bitmap?) {
        this.bitmap = bitmap
        invalidate()
    }

    private var rotate = 0f
    fun setRotate(rotate: Float) {
        if (this.rotate == rotate) return
        this.rotate = rotate
        invalidate()
    }

    private var tilt = 0f
    fun setTilt(tilt: Float) {
        if (this.tilt == tilt) return
        this.tilt = tilt
        invalidate()
    }

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

        bitmap?.let {

            val paint = Paint()
            paint.color = Color.RED

            val matrix = Matrix()
            var x = width / 2f - it.width / 2
            var y = height / 2f - it.height / 2

            if (point.x == -1 && point.y == -1) {
                x = width / 2f - it.width / 2
                y = height / 2f - it.height / 2
            } else {
                x = point.x / 2f- it.width / 2
                if (tilt > 0) {
                    y = point.y / 2f - (it.height * 0.7f) / 2
                } else {
                    y = point.y / 2f - it.height / 2
                }
            }

            matrix.setRotate(rotate, it.width / 2f, it.height / 2f)

            if (tilt > 0) {
                matrix.postScale(1f, 0.7f)
            }

            matrix.postTranslate(x, y)

            canvas.drawBitmap(it, matrix, paint)

        }

    }
}