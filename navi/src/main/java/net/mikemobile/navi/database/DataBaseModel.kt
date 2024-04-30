package net.mikemobile.sampletimer.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.mikemobile.sampletimer.database.dao.LocationFavoriteDao
import net.mikemobile.sampletimer.database.dao.RoboDao
import net.mikemobile.sampletimer.database.dao.TalkLogDao
import net.mikemobile.sampletimer.database.entity.LocationFavorite
import net.mikemobile.sampletimer.database.entity.Robo
import net.mikemobile.sampletimer.database.entity.TalkLog


/**
 * Dao =  Data Access Object
 * データへのアクセス（追加、削除、編集、取り出し）を実際に設定するためのインターフェースクラス
 */





/**
 * database
 * DAOを呼び出して使用するためのDBまとめ用クラス。
 * データの実際の取り出しは、このクラスを呼び出して使用します。
 */

@Database(entities = [
    LocationFavorite::class
    , Robo::class
//    , TalkLog::class
], version = DataBaseModel.DB_VER, exportSchema = false)
abstract class DataBaseModel : RoomDatabase() {

    companion object {
        const val DB_VER = 2
        const val DB_NAME = "robohon-database"

        fun getInstance(context: Context): DataBaseModel{
            return Room.databaseBuilder(context, DataBaseModel::class.java, DB_NAME)
                .addMigrations(object: Migration((DB_VER -1), DB_VER){
                    override fun migrate(database: SupportSQLiteDatabase) {

                        if(DB_VER == 3){

                        }

                    }
                })
                .build()
        }
    }

    //アイテム情報
    abstract fun onLocationFavoriteDao(): LocationFavoriteDao

    abstract fun onRoboDao(): RoboDao

    //abstract fun onTalkLogDao(): TalkLogDao


}