package net.mikemobile.navi.system

import net.mikemobile.databindinglib.base.ActivityNavigator
import net.mikemobile.navi.MainActivity


fun ActivityNavigator.startService() {
    activity?.let{
        (it as MainActivity).startService()
    }
}


fun ActivityNavigator.startPictureInPicture() {
    activity?.let{
        (it as MainActivity).startPictureInPicture()
    }
}


