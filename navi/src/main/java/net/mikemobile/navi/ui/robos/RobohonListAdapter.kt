package net.mikemobile.navi.ui.robos

import android.bluetooth.BluetoothDevice
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.mikemobile.navi.R
import net.mikemobile.navi.data.robo.RoboData


interface RoboListItemClickListener {
    fun clickItem(position: Int, robo: RoboData)
    fun clickConnect(position: Int, robo: RoboData)
}

class RoboListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var robo_name = view.findViewById(R.id.robo_name) as TextView
    var robo_image = view.findViewById<ImageView>(R.id.robo_image)
}


class RobohonListAdapter(var listener: RoboListItemClickListener): RecyclerView.Adapter<RoboListViewHolder>()  {
    var list = mutableListOf<RoboData>()

    var selectData: BluetoothDevice? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoboListViewHolder {
        var view: View = LayoutInflater.from(parent.context).inflate(R.layout.list_item_robo, parent,false)
        return RoboListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(viewHolder: RoboListViewHolder, position: Int) {
        var data = list[position]
        viewHolder.robo_name.text = data.name + "\n" + data.address

        if (selectData != null && selectData!!.address == data.address) {
            viewHolder.robo_image.imageTintList = ColorStateList.valueOf(Color.RED)
        } else {
            viewHolder.robo_image.imageTintList = ColorStateList.valueOf(Color.LTGRAY)
        }


        viewHolder.robo_image.setOnClickListener {
            if (selectData == null || selectData!!.address != list[position].address) {
                listener.clickConnect(position, data)
            }
        }
        viewHolder.itemView.setOnClickListener {
            listener.clickItem(position, data)
        }
    }
}