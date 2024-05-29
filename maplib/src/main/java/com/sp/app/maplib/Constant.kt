package com.sp.app.maplib

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException
import java.time.temporal.TemporalQueries.precision
import java.util.Locale


class Constant {
    companion object{

        const val ZOOM_ONE_SIZE = 0.9f

        enum class MAP_STATUS {
            MAP_LOAD,           // マップ情報の読み込み中
            MAP,                // マップ表示状態
            MAP_DIRECT_MOVE,    // 位置情報を直接指定して移動
            TOUCH_MOVE,         // タッチして地図捜査中
            ROUTE_LINE,         // ルート線描画中
            GUIDE               // 案内中
        }


        fun getDisplaySize(activity: Activity): Point {
            val display = activity.windowManager.defaultDisplay
            val point = Point()
            display.getSize(point)
            return point
        }

        fun getAddress(context: Context, latitude: Double, longitude: Double): String {

            val geocoder = Geocoder(context, Locale.getDefault())
            var addressList = mutableListOf<Address>()
            val result = StringBuilder()

            try {
                addressList = geocoder.getFromLocation(latitude, longitude, 1) as MutableList<Address>
            } catch (e: IOException) {
                android.util.Log.e("Constant","" + e.toString())
                return ""
            }

            for (address in addressList) {
                val idx = address.getMaxAddressLineIndex()

                val admin = address.adminArea
                val subAdminArea = address.subAdminArea
                val locality = address.locality
                val subLocality = address.subLocality
                val thoroughfare = address.thoroughfare
                val subThoroughfare = address.subThoroughfare
                val addressLines = address.getAddressLine(0)

                android.util.Log.d("Constant","idx:" + idx)
                android.util.Log.d("Constant","admin:" + admin)
                android.util.Log.d("Constant","subAdminArea:" + subAdminArea)
                android.util.Log.d("Constant","locality:" + locality)
                android.util.Log.d("Constant","subLocality:" + subLocality)
                android.util.Log.d("Constant","thoroughfare:" + thoroughfare)
                android.util.Log.d("Constant","subThoroughfare:" + subThoroughfare)
                android.util.Log.d("Constant","addressLines:" + addressLines)

                if(idx == 0) {
                    var parse = addressLines.split("、")
                    result.append(parsePrefecture(parse[1]))
                }else {
                    // 1番目のレコードは国名のため省略
                    for (i in 1 until idx) {
                        result.append(address.getAddressLine(i))
                    }
                }
            }

            android.util.Log.i("Constant","address:" + result.toString())
            return result.toString()
        }

        /**
         * MarkerOptionsの作成
         */
        fun createMarkerOptions(context: Context, title: String, point: LatLng, resourceId: Int, alpha:Float = 1f): MarkerOptions{
            val options = MarkerOptions()
            options.title(title)
            options.position(point)
            options.alpha(alpha)
            options.anchor(0.5f, 0.85f)//　中心位置指定

            if (resourceId == -1) {
                var icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                options.icon(icon)
            } else {
                val bmp = getImage(context, resourceId)
                bmp?.let {
                    val icon = BitmapDescriptorFactory.fromBitmap(bmp)
                    //var icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                    options.icon(icon)
                }
            }

            return options
        }

        fun getMarkerOptions(context: Context, latLng: LatLng, text: String?, resource: Int, alpha: Float = 1f): MarkerOptions {
            val options = MarkerOptions()
            if (text != null)options.title(text)
            options.position(latLng)
            getImage(context, resource)?.let {
                var icon = BitmapDescriptorFactory.fromBitmap(it)
                //var icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                options.icon(icon)
            }
            return options
        }

        var imageMap = mutableMapOf<Int, Bitmap?>()
        fun getImage(context: Context, resource_id: Int): Bitmap? {
            if(imageMap.containsKey(resource_id)){
                var bitmap = imageMap.get(resource_id)

                if(bitmap != null) {
                    return bitmap
                }
            }

            val r: Resources = context.resources
            var bitmap = BitmapFactory.decodeResource(r, resource_id)

            imageMap.set(resource_id, bitmap)

            return bitmap
        }

        fun parsePrefecture(address: String): String {
            var prefList = arrayListOf(
                "北海道",
                "青森県",
                "岩手県",
                "宮城県",
                "秋田県",
                "山形県",
                "福島県",
                "茨城県",
                "栃木県",
                "群馬県",
                "埼玉県",
                "千葉県",
                "東京都",
                "神奈川県",
                "新潟県",
                "富山県",
                "石川県",
                "福井県",
                "山梨県",
                "長野県",
                "岐阜県",
                "静岡県",
                "愛知県",
                "三重県",
                "滋賀県",
                "京都府",
                "大阪府",
                "兵庫県",
                "奈良県",
                "和歌山県",
                "鳥取県",
                "島根県",
                "岡山県",
                "広島県",
                "山口県",
                "徳島県",
                "香川県",
                "愛媛県",
                "高知県",
                "福岡県",
                "佐賀県",
                "長崎県",
                "熊本県",
                "大分県",
                "宮崎県",
                "鹿児島県",
                "沖縄県"
            )

            for(pref in prefList) {
                var split1 = address.split(pref)
                if(split1.size > 1) {
                    return pref + split1[1]
                }
            }

            return address
        }
    }

}