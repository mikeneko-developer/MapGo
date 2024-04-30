package net.mikemobile.navi.repository

import android.content.Context
import android.os.Handler
import android.util.Log
import androidx.lifecycle.MutableLiveData
import net.mikemobile.navi.core.gps.MyDate
import net.mikemobile.navi.data.old.FavoriteData
import net.mikemobile.navi.data.map.MapLocation
import net.mikemobile.navi.database.OnRoomDatabaseListener
import net.mikemobile.navi.database.model.LocationFavoriteModel
import net.mikemobile.navi.ui.navi.MapTopViewModel
import net.mikemobile.navi.util.Constant
import net.mikemobile.sampletimer.database.entity.LocationFavorite
import net.mikemobile.sampletimer.database.entity.Robo


interface DataRepositoryListener {
    fun favoriteUpdate()
    fun favoriteDelete()
}

class DataRepository(var context: Context) {
    enum class LoadToAction {
        NONE,
        SAVE,
        DELETE,
    }
    var locationFavorite = LocationFavoriteModel(context)

    var listDialogList = mutableListOf<String>()

    var favoriteList = MutableLiveData<MutableList<FavoriteData>>().apply{value = mutableListOf()}


    var favoriteEdit: FavoriteData? = null

    var listener: DataRepositoryListener? = null
    var handler = Handler()
    fun setOnDataRepositoryListener(l: DataRepositoryListener?){
        listener = l
    }

    fun loadFavoriteLocation(action: LoadToAction = LoadToAction.NONE) {
        Log.i("DataRepository" + " ADD_MAKER","loadFavoriteLocation")
        locationFavorite.read( object: OnRoomDatabaseListener{
            override fun onError(error_code: Int) {}
            override fun onDeleted() {}
            override fun onCreated(data: LocationFavorite) {}
            override fun onUpdated(data: LocationFavorite) {}
            override fun onRead(list: List<LocationFavorite>) {
                Log.i("DataRepository" + " ADD_MAKER","onRead")
                val fList = mutableListOf<FavoriteData>()
                list.forEach{
                    val favorite = FavoriteData(it.id, it.title,it.address, it.lat, it.lng)
                    fList.add(favorite)
                }
                favoriteList.postValue(fList)

                handler.post(object: Runnable{
                    override fun run() {
                        when (action) {
                            LoadToAction.SAVE,
                            LoadToAction.NONE -> {
                                Log.i("DataRepository" + " ADD_MAKER","favoriteUpdate")
                                //listener?.favoriteUpdate()
                            }
                            LoadToAction.DELETE -> {
                                listener?.favoriteDelete()
                            }
                        }
                    }
                })

            }

            override fun onCreated(data: Robo) {}
            override fun onUpdated(data: Robo) {}
            override fun onReadRobo(list: List<Robo>) {}
        })
    }

    fun saveFavorite(pointData: MapLocation) {
        var address = Constant.getAddress(context, pointData.point.latitude, pointData.point.longitude)

        var favorite = LocationFavorite()
        favorite.lat = pointData.point.latitude
        favorite.lng = pointData.point.longitude
        favorite.title = pointData.name
        favorite.address = address
        favorite.save_date = MyDate.getTimeMillis()

        locationFavorite.save(favorite, object: OnRoomDatabaseListener{
            override fun onError(error_code: Int) {}
            override fun onDeleted() {}
            override fun onRead(list: List<LocationFavorite>) {}
            override fun onCreated(data: LocationFavorite) {
                loadFavoriteLocation()
            }
            override fun onUpdated(data: LocationFavorite) {
                loadFavoriteLocation()
            }

            override fun onCreated(data: Robo) {}
            override fun onUpdated(data: Robo) {}
            override fun onReadRobo(list: List<Robo>) {}
        })
    }

    fun saveFavorite(favoriteData: FavoriteData) {
        var favorite = LocationFavorite()
        favorite.id = favoriteData.id
        favorite.title = favoriteData.name
        favorite.address = favoriteData.address
        favorite.lat = favoriteData.lat
        favorite.lng = favoriteData.lon
        favorite.save_date = MyDate.getTimeMillis()

        locationFavorite.save(favorite, object: OnRoomDatabaseListener{
            override fun onError(error_code: Int) {}
            override fun onDeleted() {}
            override fun onRead(list: List<LocationFavorite>) {}
            override fun onCreated(data: LocationFavorite) {
                loadFavoriteLocation()
            }
            override fun onUpdated(data: LocationFavorite) {
                loadFavoriteLocation()
            }

            override fun onCreated(data: Robo) {}
            override fun onUpdated(data: Robo) {}
            override fun onReadRobo(list: List<Robo>) {}
        })
    }

    fun deleteFavorite(favoriteData: FavoriteData) {
        var favorite = LocationFavorite()
        favorite.id = favoriteData.id
        favorite.title = favoriteData.name
        favorite.address = favoriteData.address
        favorite.lat = favoriteData.lat
        favorite.lng = favoriteData.lon
        favorite.save_date = MyDate.getTimeMillis()

        locationFavorite.delete(favorite, object: OnRoomDatabaseListener{
            override fun onError(error_code: Int) {}
            override fun onDeleted() {
                loadFavoriteLocation(LoadToAction.DELETE)
            }
            override fun onRead(list: List<LocationFavorite>) {}
            override fun onCreated(data: LocationFavorite) {}
            override fun onUpdated(data: LocationFavorite) {}
            override fun onCreated(data: Robo) {}
            override fun onUpdated(data: Robo) {}
            override fun onReadRobo(list: List<Robo>) {}
        })
    }



}