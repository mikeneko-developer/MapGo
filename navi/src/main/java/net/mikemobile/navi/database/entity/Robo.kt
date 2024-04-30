package net.mikemobile.sampletimer.database.entity

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName= "robo")
data class Robo(
    @PrimaryKey(autoGenerate = true)
    open var id: Int,           // データ保存用基本ID
    open var name: String,     //
    open var address: String,         //
    open var defaultFlg: Int,     // タイトル
    open var save_date: Long,         // 保存日
    open var deleteFlg: Int

): Serializable {
    constructor(): this(
        0,
        "ロボホン",
        "",
        0,
        0,
        0){
    }
}