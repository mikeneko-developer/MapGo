package net.mikemobile.navi.ui.util.custom_holizontal_view

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.mikemobile.navi.R



class VerticalListAdapter() : CustomBaseAdapter() {

    var list = mutableListOf<VerticalData>()

    override fun onCreateCustomViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        var view: View = LayoutInflater.from(parent.context).inflate(R.layout.list_item_menu, parent,false)
        return ViewHolder(view)
    }

    override fun onBindCustomViewHolder(viewHolder: ViewHolder, position: Int) {

        val addressData = list[position]


        if(position % 2 == 0){
            viewHolder.textView.setBackgroundColor(Color.YELLOW)
        }else {
            viewHolder.textView.setBackgroundColor(Color.CYAN)
        }

        viewHolder.textView.setText(addressData.name)
    }


    override fun getCustomItemCount(): Int {
        return list.size
    }
}

private class AddressDataDiffCallback : DiffUtil.ItemCallback<VerticalData>() {

    override fun areItemsTheSame(oldItem: VerticalData, newItem: VerticalData): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: VerticalData, newItem: VerticalData): Boolean {
        return oldItem == newItem
    }
}
