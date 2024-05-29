package com.sp.app.mapgo.koin

import android.widget.TextView
import androidx.databinding.BindingAdapter

object BindingAdapters {
    /**
    @JvmStatic
    @BindingAdapter("bindSrcCompat")
    fun bindSrcCompat(imageView: ImageView?, drawable: Drawable?) {
    imageView?.setImageDrawable(drawable)
    }
     */

    @JvmStatic
    @BindingAdapter("bindingAngleSize")
    fun bindingAngleSize(textView: TextView?, angle: Float?) {
        textView?.text = "" + angle + "m"
    }
}