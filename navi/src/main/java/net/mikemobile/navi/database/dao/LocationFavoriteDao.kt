package net.mikemobile.sampletimer.database.dao

import androidx.room.*
import net.mikemobile.sampletimer.database.entity.LocationFavorite


@Dao
interface LocationFavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun create(favorite: LocationFavorite):Long

    @Update
    fun update(favorite: LocationFavorite)

    @Delete
    fun delete(favorite: LocationFavorite)

    @Query("SELECT * FROM locationFavorite WHERE id = :id LIMIT 1")
    fun find(id:Int): LocationFavorite?

    @Query("SELECT * FROM locationFavorite WHERE deleteFlg = 0")
    fun findAll(): List<LocationFavorite>




    @Query("SELECT * FROM locationFavorite WHERE lat = :lat AND lng = :lng LIMIT 1")
    fun find(lat:Double, lng: Double): LocationFavorite?

}