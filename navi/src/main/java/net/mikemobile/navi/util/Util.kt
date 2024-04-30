package net.mikemobile.navi.util

import android.location.Location
import android.util.Log

fun getLocateFloat(latlng: Double):String {
    var stringFormat = String.format("%.5f", latlng.toFloat())
    return stringFormat
}
fun distanceCalc(
    startLatitude:Double,
    startLongitude:Double,
    endLatitude:Double,
    endLongitude:Double): Int {

    val results = FloatArray(3)

    Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results)
    //Log.v("TEST", "results[0]: " + results[0]); // 距離（メートル）
    //Log.v("TEST", "results[1]: " + results[1]); // 始点から終点までの方位角
    //Log.v("TEST", "results[2]: " + results[2]); // 終点から始点までの方位角

    var kiro = (results[0]/1000)
    //Log.v("TEST", "kiro: " + kiro); // 距離（キロメートル）

    return results[0].toInt()

}




fun parseRouteNavigationText(text: String): String{

    var parse_text = text.replace("<br>","\n")
    parse_text = parse_text.replace("<br/>","\n")
    parse_text = parse_text.replace("<div","\n")


    var parse = parse_text.split(">")

    var _text = ""
    for(i in 0 until parse.size){
        var parse2 = parse[i].split("<")
        _text += parse2[0]
    }

    _text = _text.replace("style=\"font-size:0.9em\"", "")

    return _text
}

/*
     * 2点間の距離（メートル）、方位角（始点、終点）を取得
     * ※配列で返す[距離、始点から見た方位角、終点から見た方位角]
     */
fun getDistance(
    x: Double,
    y: Double,
    x2: Double,
    y2: Double
): FloatArray? {
    // 結果を格納するための配列を生成
    val results = FloatArray(3)

    // 距離計算
    Location.distanceBetween(x, y, x2, y2, results)
    return results
}