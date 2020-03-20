package com.internshala.echo.Fragments

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.internshala.echo.Adapters.FavouritesAdapter
import com.internshala.echo.Adapters.MainScreenAdapter
import com.internshala.echo.Databases.EchoDatabase
import com.internshala.echo.R
import com.internshala.echo.Songs
import org.w3c.dom.Text
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 *
 */
class FavouritesFragment : Fragment() {

    var myActivity: Activity? = null
    var noFavourites: RelativeLayout? = null
    var bottomBar: RelativeLayout? = null
    var barButtonPlayPause: ImageButton? = null
    var songTitle: TextView? = null
    var recyclerView: RecyclerView? = null
    var trackPositon: Int = 0
    var favouriteContent: EchoDatabase? = null
    var visibleLayout: RelativeLayout? = null
    var getSongsList: ArrayList<Songs>? = null

    var refreshList: ArrayList<Songs>? = null
    var getListFromDatabase: ArrayList<Songs>? = null
    var favouritesAdapter: FavouritesAdapter? = null

    object Statified {
        var mediaPlayer: MediaPlayer? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favourites, container, false)
        setHasOptionsMenu(true)
        activity?.title = "Favourites"

        noFavourites = view?.findViewById(R.id.noFavourites)
        visibleLayout = view?.findViewById(R.id.visibleLayout)
        bottomBar = view?.findViewById(R.id.favBottomBar)
        barButtonPlayPause = view?.findViewById(R.id.barButtonPlayPause)
        songTitle = view?.findViewById(R.id.songTitle)
        recyclerView = view?.findViewById(R.id.fav_recycler_view)
        return view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        favouriteContent = EchoDatabase(myActivity)
        getSongsList = getSongs()

        val prefs = activity?.getSharedPreferences("ActionSort", Context.MODE_PRIVATE)
        val actionSortAscending = prefs?.getString("ActionSortAscending", "True")
        val actionSortRecent = prefs?.getString("ActionSortRecent", "True")

        if (getSongsList == null) {
            visibleLayout?.visibility = View.INVISIBLE
            noFavourites?.visibility = View.VISIBLE
        } else {
            showFavourites()
        }

        if (refreshList != null) {
            if (actionSortAscending!!.equals("True", true)) {
                Collections.sort(refreshList, Songs.Statified.nameComparator)
                favouritesAdapter?.notifyDataSetChanged()
            } else if (actionSortRecent!!.equals("True", true)) {
                Collections.sort(refreshList, Songs.Statified.dateComparator)
                favouritesAdapter?.notifyDataSetChanged()
            }
        }
        bottomBarSetup()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.main, menu)
        return
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val switcher = item?.itemId
        if (switcher == R.id.action_sort_ascending) {
            val editor1 = myActivity?.getSharedPreferences("ActionSort", Context.MODE_PRIVATE)?.edit()
            editor1?.putString("ActionSortAscending", "True")
            editor1?.putString("ActionSortRecent", "False")
            editor1?.apply()
            if (refreshList != null) {
                Collections.sort(refreshList, Songs.Statified.nameComparator)
            }
            favouritesAdapter?.notifyDataSetChanged()
            return false
        } else if (switcher == R.id.action_sort_recent) {
            val editor2 = myActivity?.getSharedPreferences("ActionSort", Context.MODE_PRIVATE)?.edit()
            editor2?.putString("ActionSortAscending", "False")
            editor2?.putString("ActionSortRecent", "True")
            editor2?.apply()
            if (refreshList != null) {
                Collections.sort(refreshList, Songs.Statified.dateComparator)
            }
            favouritesAdapter?.notifyDataSetChanged()
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
    }

    fun getSongs(): ArrayList<Songs> {

        var arrayList = ArrayList<Songs>()
        var contentResolver = myActivity?.contentResolver
        var songURI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songURI, null, null, null, null)
        if (songCursor != null && songCursor.moveToFirst()) {
            val songID = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateAdded = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            while (songCursor.moveToNext()) {
                var currentID = songCursor.getLong(songID)
                var currentTitle = songCursor.getString(songTitle)
                var currentArtist = songCursor.getString(songArtist)
                var currentData = songCursor.getString(songData)
                var currentDate = songCursor.getLong(dateAdded)

                arrayList.add(Songs(currentID, currentData, currentTitle, currentArtist, currentDate))
            }
        }
        return arrayList
    }

    fun bottomBarSetup() {
        try {
            bottomBarClickHandler()
            songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            SongPlayingFragment.Statified.mediaPlayer?.setOnCompletionListener(
                {
                    songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
                    SongPlayingFragment.Staticated.onSongComplete()
                })
            SongPlayingFragment.Statified.audioVisualization?.linkTo(SongPlayingFragment.Statified.mediaPlayer as MediaPlayer)

            if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                bottomBar?.visibility = View.VISIBLE
            } else {
                bottomBar?.visibility = View.INVISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun bottomBarClickHandler() {
        bottomBar?.setOnClickListener({
            Statified.mediaPlayer = SongPlayingFragment.Statified.mediaPlayer
            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putInt("SongID", SongPlayingFragment.Statified.currentSongHelper?.songID?.toInt() as Int)
            args.putString("SongArtist", SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            args.putString("Path", SongPlayingFragment.Statified.currentSongHelper?.songPath)
            args.putString("SongTitle", SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            args.putInt("SongPosition", SongPlayingFragment.Statified.currentSongHelper?.currentPosition as Int)
            args.putParcelableArrayList("SongData", SongPlayingFragment.Statified.fetchSongs)
            args.putString("FavBottomBar", "Successs")

            songPlayingFragment.arguments = args

            fragmentManager?.beginTransaction()?.replace(R.id.details_fragment, songPlayingFragment)
                ?.addToBackStack("SongPlayingFragment")?.commit()
        })

        barButtonPlayPause?.setOnClickListener({
            if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                SongPlayingFragment.Statified.mediaPlayer?.pause()
                trackPositon = SongPlayingFragment.Statified.mediaPlayer?.currentPosition as Int
                barButtonPlayPause?.setBackgroundResource(R.drawable.play_icon)
            } else {
                trackPositon = SongPlayingFragment.Statified.mediaPlayer?.currentPosition as Int
                SongPlayingFragment.Statified.mediaPlayer?.seekTo(trackPositon)
                SongPlayingFragment.Statified.mediaPlayer?.start()
                SongPlayingFragment.Statified.mediaPlayer?.setOnCompletionListener(
                    {
                        songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
                        SongPlayingFragment.Staticated.onSongComplete()
                    })
                barButtonPlayPause?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
    }

    fun showFavourites() {
        if (favouriteContent?.checkSize() as Int > 0) {
            refreshList = ArrayList<Songs>()
            getListFromDatabase = favouriteContent?.queryDBList()
            val fetchListFromDevice = getSongs()
            if (fetchListFromDevice != null) {
                for (i in 0..getListFromDatabase?.size as Int - 1) {
                    refreshList?.add((getListFromDatabase as ArrayList<Songs>)[i])
                }
            } else {
            }

            if (refreshList == null) {
                recyclerView?.visibility = View.INVISIBLE
                noFavourites?.visibility = View.VISIBLE
            } else {
                favouritesAdapter = FavouritesAdapter(refreshList as ArrayList<Songs>, myActivity as Context)
                favouritesAdapter?.notifyDataSetChanged()
                val mLayoutManager = LinearLayoutManager(myActivity)
                recyclerView?.layoutManager = mLayoutManager
                recyclerView?.itemAnimator = DefaultItemAnimator()
                recyclerView?.adapter = favouritesAdapter
            }
        } else {
            visibleLayout?.visibility = View.INVISIBLE
            noFavourites?.visibility = View.VISIBLE
        }
    }
}