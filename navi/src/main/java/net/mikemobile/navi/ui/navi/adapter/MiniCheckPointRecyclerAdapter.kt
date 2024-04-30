package net.mikemobile.navi.ui.navi.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import net.mikemobile.navi.R
import net.mikemobile.navi.data.map.MapCheckPoint
import net.mikemobile.navi.data.map.MapLocation
import net.mikemobile.navi.databinding.ListItemMiniCheckBinding


class MiniCheckPointRecyclerAdapter() : RecyclerView.Adapter<MiniCheckListViewHolder>() {
    lateinit var listener: OnItemClickListener

    private var items = ArrayList<MapLocation>()

    fun getSize(): Int {
        return items.size
    }

    fun getItem(position: Int): MapLocation? {
        if(items.size >= 0 && position < items.size) {
            return items[position]
        }
        return null
    }

    constructor(l: OnItemClickListener) : this() {
        setOnItemClickListener(l)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiniCheckListViewHolder {
        //setOnItemClickListener(listener)


        // DataBinding
        val binding =
            ListItemMiniCheckBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MiniCheckListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MiniCheckListViewHolder, position: Int) {
        val item = items[position]

        // データセット
        //holder.binding.setItem(item)
        holder.binding.mapLocation = item

        /**
        holder.binding.listItemMiniCheckName.text = if (item.name == "選択位置") {
            item.address
        } else {
            item.name
        }
         */

        val image = if(item.haveGoal) {
            R.drawable.ic_goal
        } else {
            R.drawable.ic_via
        }
        holder.binding.listItemMiniCheckMarker.setImageResource(image)

        //ClickListenerのセットはココ！
        holder.binding.listItemMiniCheckFrame.setOnClickListener {
            //処理はRecordModel#itemClickに実装
            listener.onItemClick(holder.adapterPosition, items[holder.adapterPosition])
        }

        // Viewへの反映を即座に行う
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, point: MapLocation)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }


    fun setList(items: ArrayList<MapLocation>) {
        this.items = items
        this.notifyDataSetChanged()
    }

    fun addItem(checkPoint: MapLocation) {
        items.add(checkPoint)

        this.notifyItemInserted(items.size - 1)
    }

    fun addItem(position: Int, checkPoint: MapLocation) {

        if (items.size > 0) {

            items.add(position, checkPoint)

            for(i in 0 until items.size - 1) {
                if (items[i].haveGoal) {
                    val item = items[i]
                    item.haveGoal = false
                    items.set(i, item)
                }
                this.notifyItemChanged(i)
            }
            this.notifyItemInserted(position)

        } else {

            items.add(position, checkPoint)
            this.notifyItemInserted(position)
        }
    }

    private fun getPosition(checkPoint: MapLocation): Int {
        for(i in 0 until items.size) {
            if (checkPoint.address == items[i].address ||
                checkPoint.point.latitude == items[i].point.latitude ||
                checkPoint.point.longitude == items[i].point.longitude) {
                return i
            }
        }
        return -1
    }

    fun changeItem(checkPoint: MapLocation) {
        if (checkPoint.haveGoal) {
            // 目的地に変更された
            val position = getPosition(checkPoint)
            if (position >= 0) {
                // 現在目的地のデータを経由地に変える
                val prevGoal = items[items.size - 1]
                prevGoal.haveGoal = false
                items.set(items.size -1, prevGoal)

                // 表示更新

                // 位置を変更する
                items.removeAt(position)
                items.add(checkPoint)

                this.notifyItemMoved(position, items.size - 1)
                this.notifyItemRangeChanged(0, items.size)
            }
        } else {
            // 経由地に変更された

            // 選択されたのアイテムを更新する
            val position = getPosition(checkPoint)
            items.set(position, checkPoint)


            // 現在最後の経由地となっているデータを目的地に変える
            val goalPosition = items.size - 2

            val nextGoal = items[goalPosition]
            nextGoal.haveGoal = true

            items.removeAt(goalPosition)
            items.add(nextGoal)

            // 表示更新
            this.notifyItemRangeChanged(0, items.size)

        }

    }

    fun deleteItem(checkPoint: MapLocation) {
        val position = getPosition(checkPoint)
        if (position >= 0) {
            items.removeAt(position)
            this.notifyItemRemoved(position)
        }

        if (items.size > 0) {
            val nextGoal = items[items.size - 1]
            nextGoal.haveGoal = true
            items.set(items.size - 1, nextGoal)
        }
        //this.notifyItemRangeChanged(0, items.size)

    }

    fun deleteItem(fromPos: Int): MapLocation? {
        var item = items[fromPos]
        items.removeAt(fromPos)
        notifyItemRemoved(fromPos)
        if (items.size > 0 && !items[items.size - 1].haveGoal) {
            val nextGoal = items[items.size - 1]
            nextGoal.haveGoal = true
            items.set(items.size - 1, nextGoal)


            //this.notifyItemChanged(items.size - 1)
        }
        return item
    }

    fun moveItem(fromPos: Int, toPos:Int) {
        val item1 = items[fromPos]
        val fromHaveGoal = item1.haveGoal
        val item2 = items[toPos]
        val toHaveGoal = item2.haveGoal

        item1.haveGoal = toHaveGoal
        item2.haveGoal = fromHaveGoal

        items[toPos] = item1
        items[fromPos] = item2

        //this.notifyItemChanged(toPos)
        //this.notifyItemChanged(fromPos)

        this.notifyItemMoved(fromPos, toPos)
    }

}