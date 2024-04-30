package net.mikemobile.navi.ui.util.custom_holizontal_view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.mikemobile.navi.R
import net.mikemobile.navi.data.map.MapCheckPoint
import net.mikemobile.navi.data.old.StepItem
import net.mikemobile.navi.util.parseRouteNavigationText


class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var textView = view.findViewById(R.id.textView) as TextView
    var scroll_frame = view.findViewById(R.id.scroll_frame) as LinearLayout
}


class HorizontalListAdapter : CustomBaseAdapter() {
    var list = mutableListOf<MapCheckPoint>()

    override fun onCreateCustomViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view: View = LayoutInflater.from(parent.context).inflate(R.layout.list_item_holizontal, parent,false)
        return ViewHolder(view)
    }

    override fun onBindCustomViewHolder(viewHolder: ViewHolder, position: Int) {
        val data = list[position]
        viewHolder.textView.setText(parseRouteNavigationText(data.html_instructions))
        //viewHolder.textView.setText(data.html_instructions)

    }


    override fun getCustomItemCount(): Int {
        return list.size
    }
}
