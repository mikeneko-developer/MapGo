package net.mikemobile.navi.ui.navi.favorite

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.mikemobile.navi.R
import net.mikemobile.navi.data.old.FavoriteData


interface FavoriteListItemClickListener {
    fun clickItem(position: Int, favoriteData: FavoriteData)
    fun clickSelect(position: Int, favoriteData: FavoriteData)
}

class FavoriteListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var title = view.findViewById(R.id.favorite_name) as TextView
    var address = view.findViewById(R.id.favorite_address) as TextView
    var btnEdit = view.findViewById(R.id.favorite_edit) as ImageButton
}


class FavoriteListAdapter(var listener: FavoriteListItemClickListener): RecyclerView.Adapter<FavoriteListViewHolder>()  {
    var list = mutableListOf<FavoriteData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteListViewHolder {
        var view: View = LayoutInflater.from(parent.context).inflate(R.layout.list_item_favorite, parent,false)
        return FavoriteListViewHolder(
            view
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(viewHolder: FavoriteListViewHolder, position: Int) {
        var data = list[position]
        viewHolder.title.text = data.name
        viewHolder.address.text = data.address
        viewHolder.address.text = data.address


        viewHolder.itemView.setOnClickListener {
            listener.clickSelect(position, data)
        }
        viewHolder.btnEdit.setOnClickListener {
            listener.clickItem(position, data)
        }
    }
}