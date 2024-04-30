package net.mikemobile.sampletimer.database.entity

import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "locationFavorite")
data class LocationFavorite(
    @PrimaryKey(autoGenerate = true)
    open var id: Int,           // データ保存用基本ID
    open var lat: Double,     //
    open var lng: Double,         //
    open var title: String,     // タイトル
    open var address: String,

    open var save_date: Long,         // 保存日
    open var deleteFlg: Int

    ): Serializable {
    constructor(): this(
        0,
        0.0,
        0.0,
        "",
        "",
        0,
        0){
    }
}