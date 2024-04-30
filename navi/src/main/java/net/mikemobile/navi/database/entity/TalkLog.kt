package net.mikemobile.sampletimer.database.entity

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "talkLog")
data class TalkLog(
    @PrimaryKey(autoGenerate = true)
    open var id: Int,           // データ保存用基本ID
    open var talk: String,     //
    open var emotion_id: Int,
    open var motion_id: Int,
    open var save_date: Long,// 保存日
    open var deleteFlg: Int

): Serializable {
    constructor(): this(
        0,
        "",
        0,
        0,
        0,
        0){
    }
}