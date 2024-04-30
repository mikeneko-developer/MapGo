package net.mikemobile.sampletimer.database.dao

import androidx.room.*
import net.mikemobile.sampletimer.database.entity.LocationFavorite
import net.mikemobile.sampletimer.database.entity.Robo
import net.mikemobile.sampletimer.database.entity.TalkLog


@Dao
interface TalkLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun create(talkLog: TalkLog):Long

    @Update
    fun update(talkLog: TalkLog)

    @Delete
    fun delete(talkLog: TalkLog)

    @Query("SELECT * FROM robo WHERE id = :id LIMIT 1")
    fun find(id:Int): TalkLog?

    @Query("SELECT * FROM robo WHERE deleteFlg = 0 LIMIT 100")
    fun findAll(): List<TalkLog>

}