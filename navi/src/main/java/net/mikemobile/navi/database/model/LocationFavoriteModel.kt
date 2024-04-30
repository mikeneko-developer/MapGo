package net.mikemobile.navi.database.model

import android.content.Context
import io.reactivex.Completable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import net.mikemobile.navi.database.OnRoomDatabaseListener
import net.mikemobile.sampletimer.database.DataBaseModel
import net.mikemobile.sampletimer.database.entity.LocationFavorite
import kotlin.concurrent.thread

class LocationFavoriteModel (context: Context) {
    var model = DataBaseModel.getInstance(context)


    fun create(favoriteData: LocationFavorite, listener: OnRoomDatabaseListener){
        thread {
            var res = model.onLocationFavoriteDao().create(favoriteData)
            if(res == 0L){
                // エラー
                listener.onError(-1000)
            }else {
                favoriteData.id = res.toInt()
                listener.onCreated(favoriteData)
            }
        }.join()
    }

    fun update(favoriteData: LocationFavorite, listener: OnRoomDatabaseListener) {
        thread {
            model.onLocationFavoriteDao().update(favoriteData)
            listener.onUpdated(favoriteData)
        }.start()
    }

    fun delete(favoriteData: LocationFavorite, listener: OnRoomDatabaseListener) {
        Completable.fromCallable {
            model.runInTransaction {
                // Roomの処理をここで書く
                model.onLocationFavoriteDao().delete(favoriteData)

            }
        }.subscribeOn(Schedulers.io()).subscribeBy {
            //
            listener.onDeleted()
        }
    }

    fun save(favoriteData: LocationFavorite, listener: OnRoomDatabaseListener) {
        var res: Long = 0L

        var update = false
        Completable.fromCallable {
            model.runInTransaction {
                // Roomの処理をここで書く

                var item = model.onLocationFavoriteDao().find(favoriteData.lat, favoriteData.lng)
                if(item == null){
                    res = model.onLocationFavoriteDao().create(favoriteData)

                }else {
                    model.onLocationFavoriteDao().update(favoriteData)

                    update = true
                }

            }
        }.subscribeOn(Schedulers.io()).subscribeBy {
            //

            if(update){
                listener.onUpdated(favoriteData)
            }else if(res == 0L){
                // エラー
                listener.onError(-1000)
            }else {
                favoriteData.id = res.toInt()
                listener.onCreated(favoriteData)
            }
        }

    }

    fun read(listener: OnRoomDatabaseListener){
        var list = mutableListOf<LocationFavorite>()

        Completable.fromCallable {
            model.runInTransaction {
                // Roomの処理をここで書く

                list = (model.onLocationFavoriteDao().findAll() as MutableList<LocationFavorite>)

            }
        }.subscribeOn(Schedulers.io()).subscribeBy {
            //

            listener.onRead(list)
        }
    }
}