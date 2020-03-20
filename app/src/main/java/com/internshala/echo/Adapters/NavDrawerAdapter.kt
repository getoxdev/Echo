package com.internshala.echo.Adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.internshala.echo.Activities.MainActivity
import com.internshala.echo.Fragments.AboutUsFragment
import com.internshala.echo.Fragments.FavouritesFragment
import com.internshala.echo.Fragments.MainScreenFragment
import com.internshala.echo.Fragments.SettingsFragment
import com.internshala.echo.R

class NavDrawerAdapter(_contentList: ArrayList<String>, _getImages: IntArray, _context: Context) :
    RecyclerView.Adapter<NavDrawerAdapter.NavViewHolder>() {
    var contentList: ArrayList<String>? = null
    var getImages: IntArray? = null
    var mContext: Context? = null

    init {
        this.contentList = _contentList
        this.getImages = _getImages
        this.mContext = _context
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): NavViewHolder {
        var itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_custom_navdrawer, parent, false)

        val navViewHolder = NavViewHolder(itemView)
        return navViewHolder
    }

    override fun getItemCount(): Int {
        return (contentList as ArrayList).size
    }

    override fun onBindViewHolder(holder: NavViewHolder, position: Int) {
        holder?.getIcon?.setBackgroundResource(getImages?.get(position) as Int)
        holder?.getText?.setText(contentList?.get(position))
        holder?.contentHolder?.setOnClickListener({
            if (position == 0) {
                val mainScreenFragment = MainScreenFragment()
                (mContext as MainActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, mainScreenFragment)
                    .addToBackStack("MainScreen")
                    .commit()
            } else if (position == 1) {
                val favouritesFragment = FavouritesFragment()
                (mContext as MainActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, favouritesFragment)
                    .addToBackStack("Favourites")
                    .commit()
            } else if (position == 2) {
                val settingsFragment = SettingsFragment()
                (mContext as MainActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, settingsFragment)
                    .addToBackStack("Settings")
                    .commit()
            } else if (position == 3) {
                val aboutUsFragment = AboutUsFragment()
                (mContext as MainActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment, aboutUsFragment)
                    .addToBackStack("AboutUs")
                    .commit()
            }
            MainActivity.Statified.drawerLayout?.closeDrawers()
        })
    }

    class NavViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var getIcon: ImageView? = null
        var getText: TextView? = null
        var contentHolder: RelativeLayout? = null

        init {
            getIcon = itemView?.findViewById(R.id.icon_navdrawer)
            getText = itemView?.findViewById(R.id.text_navdrawer)
            contentHolder = itemView?.findViewById(R.id.navdrawer_item_holder)
        }
    }
}