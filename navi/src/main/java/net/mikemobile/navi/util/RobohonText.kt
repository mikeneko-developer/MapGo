package net.mikemobile.navi.util

class RobohonText {
    fun changeText(text: String): String {

        var returnText = text
        if (returnText.indexOf("南に進む") != -1) {
            returnText = returnText.replace("南に進む","南に進むよ")
        } else if (returnText.indexOf("北に進む") != -1) {
            returnText = returnText.replace("北に進む","南に進むよ")
        } else if (returnText.indexOf("西に進む") != -1) {
            returnText = returnText.replace("西に進む","南に進むよ")
        } else if (returnText.indexOf("東に進む") != -1) {
            returnText = returnText.replace("東に進む","南に進むよ")
        } else {
            returnText = returnText.replace("進む","そのまま進むよ")
        }

        returnText = returnText.replace("向かう","向かうよ")
        returnText = returnText.replace("入る","入るよ")
        returnText = returnText.replace("歩く","歩くよ")
        returnText = returnText.replace("使用が制限されている道路","")
        returnText = returnText.replace("左折する","左に曲がるよ")
        returnText = returnText.replace("右折する","右に曲がるよ")
        returnText = returnText.replace("直進する","まっすぐ進むよ")
        returnText = returnText.replace("目的地付近です","ゴールが近くだよ")
        returnText = returnText.replace("目的地は前方右側です","ゴールは前方右側だよ")
        returnText = returnText.replace("目的地は前方左側です","ゴールは前方左側だよ")

        returnText = returnText.replace("\r\n","\n")
        returnText = returnText.replace("\r","\n")


        if(true) {
            var parseText = returnText.split("style")
            if(parseText.size > 1) {
                returnText = parseText[0]
            }
        }

        if (true) {
            var parseText = returnText.split("\n")

            if(parseText.size > 2) {
                returnText = parseText[0] + "、" + parseText[parseText.size - 1]
            }else if(parseText.size == 2) {
                returnText = parseText[0]
            }
        }

        return returnText
    }
}