package net.mikemobile.navi.ui.util.custom_holizontal_view

import android.view.ViewGroup
import android.widget.LinearLayout.VERTICAL
import androidx.recyclerview.widget.RecyclerView

abstract class CustomBaseAdapter: RecyclerView.Adapter<ViewHolder>()  {
    abstract fun onCreateCustomViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    abstract fun onBindCustomViewHolder(viewHolder: ViewHolder, position: Int)
    abstract fun getCustomItemCount(): Int

    var scrollPositionX = -1
    var scrollPositionY = -1
    var oneItemHeight = 0
    var selectPosition = -1
    var orientation = VERTICAL
    //var orientation = HORIZONTAL

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return onCreateCustomViewHolder(parent, viewType)
    }

    override fun getItemCount(): Int {
        return getCustomItemCount()
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {


        if(orientation == VERTICAL) {
            var scroll = -(scrollPositionY.toFloat())

            var scrollToPosi = 0
            if (position > 0) scrollToPosi = (scroll / (oneItemHeight * position)).toInt()

            if (position > 0 && scrollToPosi == 0) {
                scroll = (oneItemHeight * position).toFloat() - scroll
            } else {
                scroll = scroll - (oneItemHeight * position)
            }

            var move = scroll / 3

            var updown = (move / 4)
            if (position > 0 && scrollToPosi == 0) {
                updown = -(move / 4)
            }

            if (orientation == VERTICAL) {
                //viewHolder.itemView.translationX = move
                //viewHolder.itemView.translationY = updown
                viewHolder.itemView.alpha = 1f - ((move / 2.5f) / 100f)
            }
        }

        if(oneItemHeight != -1) {
            //viewHolder.scroll_frame.layoutParams.height = setHeightSize

            if(position == selectPosition){
                viewHolder.itemView.layoutParams.height = oneItemHeight
            }else {
                //var min = setHeightSize / 3 * 2
                //var smole = setHeightSize * ((100 - (scroll)).toFloat() / 100).toInt()
                //if (smole < min) smole = min
                //viewHolder.itemView.layoutParams.height = smole

                //heightSize = smole
                viewHolder.itemView.layoutParams.height = oneItemHeight
            }
        }

        onBindCustomViewHolder(viewHolder, position)
    }

}