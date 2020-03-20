package com.internshala.echo.Fragments


import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.internshala.echo.Adapters.MainScreenAdapter
import com.internshala.echo.R
import com.internshala.echo.Songs
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 *
 */
class MainScreenFragment : Fragment() {

    var getSongsList: ArrayList<Songs>? = null
    var bottomBar: RelativeLayout? = null
    var barButtonPlayPause: ImageButton? = null
    var songTitle: TextView? = null
    var visibleLayout: RelativeLayout? = null
    var noSongs: RelativeLayout? = null
    var contentMain: RecyclerView? = null
    var myActivity: Activity? = null
    var mainScreenAdapter: MainScreenAdapter? = null
    var trackPositon: Int? = 0

    object Statified {
        var mediaPlayer: MediaPlayer? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main_screen, container, false)
        setHasOptionsMenu(true)
        activity?.title = "All Songs"

        visibleLayout = view?.findViewById(R.id.visibleLayout)
        bottomBar = view?.findViewById(R.id.bottomBar)
        noSongs = view?.findViewById(R.id.noSongs)
        contentMain = view?.findViewById(R.id.contentMain)
        barButtonPlayPause = view?.findViewById(R.id.barButtonPlayPause)
        songTitle = view?.findViewById(R.id.songTitle)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        getSongsList = getSongs()

        val prefs = activity?.getSharedPreferences("ActionSort", Context.MODE_PRIVATE)
        val actionSortAscending = prefs?.getString("ActionSortAscending", "True")
        val actionSortRecent = prefs?.getString("ActionSortRecent", "True")

        if (getSongsList == null) {
            visibleLayout?.visibility = View.INVISIBLE
            noSongs?.visibility = View.VISIBLE
        } else {
            mainScreenAdapter = MainScreenAdapter(getSongsList as ArrayList<Songs>, myActivity as Context)
            val mLayoutManager = LinearLayoutManager(myActivity)
            contentMain?.layoutManager = mLayoutManager
            contentMain?.itemAnimator = DefaultItemAnimator()
            contentMain?.adapter = mainScreenAdapter
        }

        if (getSongsList != null) {
            if (actionSortAscending!!.equals("True", true)) {
                Collections.sort(getSongsList, Songs.Statified.nameComparator)
                mainScreenAdapter?.notifyDataSetChanged()
            } else if (actionSortRecent!!.equals("True", true)) {
                Collections.sort(getSongsList, Songs.Statified.dateComparator)
                mainScreenAdapter?.notifyDataSetChanged()
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
            if (getSongsList != null) {
                Collections.sort(getSongsList, Songs.Statified.nameComparator)
            }
            mainScreenAdapter?.notifyDataSetChanged()
            return false
        } else if (switcher == R.id.action_sort_recent) {
            val editor2 = myActivity?.getSharedPreferences("ActionSort", Context.MODE_PRIVATE)?.edit()
            editor2?.putString("ActionSortAscending", "False")
            editor2?.putString("ActionSortRecent", "True")
            editor2?.apply()
            if (getSongsList != null) {
                Collections.sort(getSongsList, Songs.Statified.dateComparator)
            }
            mainScreenAdapter?.notifyDataSetChanged()
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
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
            SongPlayingFragment.Statified.mediaPlayer?.setOnCompletionListener {
                SongPlayingFragment.Staticated.onSongComplete()
                songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            }
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
            MainScreenFragment.Statified.mediaPlayer = SongPlayingFragment.Statified.mediaPlayer
            val songPlayingFragment = SongPlayingFragment()
            var args = Bundle()
            args.putInt("SongID", SongPlayingFragment.Statified.currentSongHelper?.songID?.toInt() as Int)
            args.putString("SongArtist", SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            args.putString("Path", SongPlayingFragment.Statified.currentSongHelper?.songPath)
            args.putString("SongTitle", SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            args.putInt("SongPosition", SongPlayingFragment.Statified.currentSongHelper?.currentPosition as Int)
            args.putParcelableArrayList("SongData", SongPlayingFragment.Statified.fetchSongs)
            args.putString("MainBottomBar", "Successs")

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
                SongPlayingFragment.Statified.mediaPlayer?.seekTo(trackPositon as Int)
                SongPlayingFragment.Statified.mediaPlayer?.start()
                barButtonPlayPause?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
    }
}
