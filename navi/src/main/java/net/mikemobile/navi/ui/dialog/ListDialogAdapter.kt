package net.mikemobile.navi.ui.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.mikemobile.navi.R

interface OnListDialogItemClickListener {
    fun onClickItem(position: Int)
}

class ListDialogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var textview = view.findViewById(R.id.list_item_text) as TextView
    var lienarlayout = view.findViewById(R.id.list_item_frame) as LinearLayout
}


class ListDialogAdapter(val listener: OnListDialogItemClickListener): RecyclerView.Adapter<ListDialogViewHolder>()  {
    var list = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListDialogViewHolder {
        var view: View = LayoutInflater.from(parent.context).inflate(R.layout.dialog_list_item, parent,false)
        return ListDialogViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(viewHolder: ListDialogViewHolder, position: Int) {
        viewHolder.textview.text = list[position]

        viewHolder.lienarlayout.setOnClickListener{
            listener.onClickItem(position)
        }
    }
}