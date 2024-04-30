package com.sp.app.maplib.data

import com.google.android.gms.maps.model.LatLng
import com.sp.app.maplib.util.distanceCalc

data class ThreePoint(
    val point1: LatLng,
    var point2: LatLng?,
    var point3: LatLng?
) {
    companion object {
    }


    fun getDistance(current: LatLng): Float {
        val distance_next = distanceCalc(
            current.latitude, current.longitude,
            point1.latitude, point1.longitude
        )

        return distance_next
    }
    fun getNextNextDistance(current: LatLng): Float {

        val distance_next_next = distanceCalc(
            current.latitude, current.longitude,
            point2!!.latitude, point2!!.longitude
        )

        return distance_next_next
    }

    fun approachingPoint(current: LatLng): Boolean {

        // 現在地から次のチェックポイントまでの距離
        val distance_next = distanceCalc(
            current.latitude, current.longitude,
            point1.latitude, point1.longitude
        )

        if (distance_next <= 10) {
            // 10m以内に近づいたら近づいた判定
            android.util.Log.i("ThreePoint", "次のチェックポイントに近づきました")
            return true
        }

        android.util.Log.i("ThreePoint", "まだチェックポイントに近づいていません")
        return false
    }

    fun checkNextNextPoint(current: LatLng): Boolean {
        if (point2 == null) return false

        // 現在地から次のチェックポイントまでの距離
        val distance_next = distanceCalc(
            current.latitude, current.longitude,
            point1.latitude, point1.longitude
        )

        val distance_next_next = distanceCalc(
            current.latitude, current.longitude,
            point2!!.latitude, point2!!.longitude
        )

        if (distance_next_next < distance_next) {
            android.util.Log.i("ThreePoint", "次の次のチェックポイントに変更")
            // 10m以内に近づいたら近づいた判定
            return true
        }
        android.util.Log.i("ThreePoint", "まだチェックポイントに近づいていません")
        return false
    }

    fun checkOtherNextPoint(current: LatLng): Boolean {
        if (point2 == null) return false
        if (point3 == null) return false

        // 3点の面積（次のチェックポイント
        val area1 = getArea(
            current.latitude,
            current.longitude,
            point1.latitude,
            point1.longitude,
            point2!!.latitude,
            point2!!.longitude)

        val area2 = getArea(
            current.latitude,
            current.longitude,
            point2!!.latitude,
            point2!!.longitude,
            point3!!.latitude,
            point3!!.longitude)

        android.util.Log.i("ThreePoint", "area1:" + area1 + " area2:" + area2)
        // 3点の面積のサイズが次の次のチェックポイント側のサイズが小さい
        if (area1 > area2) {
            return true
        }
        android.util.Log.i("ThreePoint", "まだチェックポイントに近づいていません")
        return false
    }

    private fun getArea(ax: Double, ay: Double, bx: Double, by: Double,  cx: Double, cy: Double): Double {
        return Math.abs((cx-bx)*(ay-by)-(ax-bx)*(cy-by))/2
    }
}