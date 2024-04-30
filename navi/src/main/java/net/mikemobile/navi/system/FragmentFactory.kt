package net.mikemobile.navi.system

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import net.mikemobile.databindinglib.base.BaseFragment
import net.mikemobile.databindinglib.base_drawer.BaseNavigationDrawerFragmentFactory
import net.mikemobile.navi.ui.dialog.ListDialog
import net.mikemobile.navi.ui.dialog.SelectButtonDialog
import net.mikemobile.navi.ui.map.MapFragment
import net.mikemobile.navi.ui.menu.MapMenuFragment
import net.mikemobile.navi.ui.menu.MenuFragment
import net.mikemobile.navi.ui.menu.RoboMenuFragment
import net.mikemobile.navi.ui.navi.favorite.FavoriteListFragment
import net.mikemobile.navi.ui.navi.MapGuideFragment
import net.mikemobile.navi.ui.navi.MapRouteFragment
import net.mikemobile.navi.ui.navi.MapTopFragment
import net.mikemobile.navi.ui.navi.favorite.FavoriteEditFragment
import net.mikemobile.navi.ui.robos.RoboListFragment
import net.mikemobile.navi.ui.robos.TalkFragment

class FragmentFactory : BaseNavigationDrawerFragmentFactory() {

    override fun create(tag: String, bundle : Bundle?) : BaseFragment {
        return when(tag) {
            MapFragment.TAG -> MapFragment.newInstance()
            MapTopFragment.TAG -> MapTopFragment.newInstance()
            MapRouteFragment.TAG -> MapRouteFragment.newInstance()
            MapGuideFragment.TAG -> MapGuideFragment.newInstance()
            FavoriteListFragment.TAG -> FavoriteListFragment.newInstance()
            FavoriteEditFragment.TAG -> FavoriteEditFragment.newInstance()

            RoboListFragment.TAG -> RoboListFragment.newInstance()
            TalkFragment.TAG -> TalkFragment.newInstance()

            MenuFragment.TAG -> MenuFragment.newInstance()
            MapMenuFragment.TAG -> MapMenuFragment.newInstance()
            RoboMenuFragment.TAG -> RoboMenuFragment.newInstance()
            else -> super.create(tag,bundle)
        }
    }
    override fun createDialog(tag: String, bundle : Bundle?) : DialogFragment {
        return when(tag) {
            SelectButtonDialog.TAG -> SelectButtonDialog.newInstance()
            ListDialog.TAG -> ListDialog.newInstance()
            else -> super.createDialog(tag,bundle)
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile private var INSTANCE: FragmentFactory? = null


    }
    override fun getInstance() =
        INSTANCE ?: synchronized(
            FragmentFactory::class.java) {
            INSTANCE
                ?: FragmentFactory().also { INSTANCE = it }
        }

    override fun destroyInstance() {
        INSTANCE = null
    }
}