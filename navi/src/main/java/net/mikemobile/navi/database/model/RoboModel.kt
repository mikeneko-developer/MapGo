package net.mikemobile.navi.database.model

import android.content.Context
import io.reactivex.Completable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import net.mikemobile.navi.database.OnRoomDatabaseListener
import net.mikemobile.sampletimer.database.DataBaseModel
import net.mikemobile.sampletimer.database.entity.LocationFavorite
import net.mikemobile.sampletimer.database.entity.Robo
import kotlin.concurrent.thread

class RoboModel (context: Context) {
    var model = DataBaseModel.getInstance(context)

    fun save(robo: Robo, listener: OnRoomDatabaseListener) {
        var res = 0L

        var update = false
        Completable.fromCallable {
            model.runInTransaction {
                // Roomの処理をここで書く

                if(robo.id == 0){
                    res = model.onRoboDao().create(robo)

                }else {
                    model.onRoboDao().update(robo)
                    update = true
                }

            }
        }.subscribeOn(Schedulers.io()).subscribeBy {
            //

            if(update){
                listener.onUpdated(robo)
            }else if(res == 0L){
                // エラー
                listener.onError(-1000)
            }else {
                robo.id = res.toInt()
                listener.onCreated(robo)
            }
        }

    }

    fun read(listener: OnRoomDatabaseListener){
        var list = mutableListOf<Robo>()

        Completable.fromCallable {
            model.runInTransaction {
                // Roomの処理をここで書く

                list = (model.onRoboDao().findAll() as MutableList<Robo>)

            }
        }.subscribeOn(Schedulers.io()).subscribeBy {
            //
            listener.onReadRobo(list)
        }
    }
}