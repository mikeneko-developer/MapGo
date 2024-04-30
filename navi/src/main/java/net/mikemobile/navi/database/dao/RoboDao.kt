package net.mikemobile.sampletimer.database.dao

import androidx.room.*
import net.mikemobile.sampletimer.database.entity.LocationFavorite
import net.mikemobile.sampletimer.database.entity.Robo


@Dao
interface RoboDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun create(robo: Robo):Long

    @Update
    fun update(robo: Robo)

    @Delete
    fun delete(robo: Robo)

    @Query("SELECT * FROM robo WHERE id = :id LIMIT 1")
    fun find(id:Int): Robo?

    @Query("SELECT * FROM robo WHERE deleteFlg = 0")
    fun findAll(): List<Robo>


    @Query("SELECT * FROM robo WHERE defaultFlg = 1 LIMIT 1")
    fun findDefault(): Robo?

}