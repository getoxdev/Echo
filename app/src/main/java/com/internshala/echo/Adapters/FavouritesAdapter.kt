package com.internshala.echo.Adapters

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.internshala.echo.Fragments.SongPlayingFragment
import com.internshala.echo.R
import com.internshala.echo.Songs

class FavouritesAdapter(_songDetails: ArrayList<Songs>, _context: Context) :
    RecyclerView.Adapter<FavouritesAdapter.MyViewHolder>() {

    var songDetails: ArrayList<Songs>? = null
    var mContext: Context? = null

    init {
        this.songDetails = _songDetails
        this.mContext = _context
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val songObject = songDetails?.get(position)
        holder.trackTitle?.text = songObject?.songTitle
        holder.trackArtist?.text = songObject?.songArtist
        holder.contentRow?.setOnClickListener({
            try {
                if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                    SongPlayingFragment.Statified.mediaPlayer?.stop()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putInt("SongID", songObject?.songID?.toInt() as Int)
            args.putString("SongArtist", songObject?.songArtist)
            args.putString("Path", songObject?.songData)
            args.putString("SongTitle", songObject?.songTitle)
            args.putInt("SongPosition", position)
            args.putParcelableArrayList("SongData", songDetails)

            songPlayingFragment.arguments = args
            (mContext as FragmentActivity).supportFragmentManager
                .beginTransaction()
                .replace(R.id.details_fragment, songPlayingFragment)
                .addToBackStack("SongPlayingFragmentFavourite")
                .commit()
        })

    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent?.context).inflate(R.layout.row_custom_mainscreen_adapter, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        if (songDetails == null) {
            return 0
        } else {
            return (songDetails as ArrayList<Songs>).size
        }
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var trackTitle: TextView? = null
        var trackArtist: TextView? = null
        var contentRow: RelativeLayout? = null

        init {
            trackTitle = view.findViewById<TextView>(R.id.trackTitle)
            trackArtist = view.findViewById<TextView>(R.id.trackArtist)
            contentRow = view.findViewById<RelativeLayout>(R.id.contentRow)
        }
    }
}
