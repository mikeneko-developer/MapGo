package net.mikemobile.navi.ui.util.custom_holizontal_view

interface CustomRecyclerViewListener{
    fun onRendering(width:Int,height:Int)
    fun onSelectPosition(position: Int)
}

interface OnSnapPositionChangeListener {
    fun onSnapPositionProcessing()
    fun onSnapPositionChange(position: Int, isChanged: Boolean)
}
