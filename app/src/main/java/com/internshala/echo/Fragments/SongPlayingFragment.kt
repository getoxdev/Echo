package com.internshala.echo.Fragments

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.DropBoxManager
import android.os.Handler
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.*
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.internshala.echo.CurrentSongHelper
import com.internshala.echo.Databases.EchoDatabase
import com.internshala.echo.Fragments.SongPlayingFragment.Staticated.onSongComplete
import com.internshala.echo.Fragments.SongPlayingFragment.Staticated.playNext
import com.internshala.echo.Fragments.SongPlayingFragment.Staticated.processInformation
import com.internshala.echo.Fragments.SongPlayingFragment.Staticated.updateTextViews
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.audioVisualization
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.buttonFav
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.buttonLoop
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.buttonNext
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.buttonPlayPause
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.buttonPrevious
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.buttonShuffle
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.currentPosition
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.currentSongHelper
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.endTimeText
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.favouriteContent
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.fetchSongs
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.glView
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.mSensorListener
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.mSensorManager
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.mediaPlayer
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.myActivity
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.seekBar
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.songArtistText
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.songTitleText
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.startTimeText
import com.internshala.echo.Fragments.SongPlayingFragment.Statified.updateSongTime
import com.internshala.echo.R
import com.internshala.echo.Songs
import java.lang.Exception
import java.net.URI
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * A simple [Fragment] subclass.
 *
 */
class SongPlayingFragment : Fragment() {

    object Statified {
        var myActivity: Activity? = null
        var mediaPlayer: MediaPlayer? = null
        var startTimeText: TextView? = null
        var endTimeText: TextView? = null
        var buttonPlayPause: ImageButton? = null
        var buttonPrevious: ImageButton? = null
        var buttonNext: ImageButton? = null
        var buttonLoop: ImageButton? = null
        var seekBar: SeekBar? = null
        var buttonShuffle: ImageButton? = null
        var songArtistText: TextView? = null
        var songTitleText: TextView? = null
        var currentSongHelper: CurrentSongHelper? = null
        var currentPosition: Int? = 0
        var fetchSongs: ArrayList<Songs>? = null
        var audioVisualization: AudioVisualization? = null
        var glView: GLAudioVisualizationView? = null
        var buttonFav: ImageButton? = null
        var favouriteContent: EchoDatabase? = null
        var updateSongTime = object : Runnable {
            override fun run() {
                val getCurrent = mediaPlayer?.currentPosition
                var mMinutes = (getCurrent?.div(1000))?.div(60)
                var mSeconds = (getCurrent?.div(1000))?.rem(60)

                startTimeText?.setText(String.format("%d:%d", mMinutes, mSeconds))
                seekBar?.setProgress(getCurrent as Int)
                Handler().postDelayed(this, 1000)
            }
        }
        var mSensorManager: SensorManager? = null
        var mSensorListener: SensorEventListener? = null
        var MY_PREFS_NAME = "ShakeFeature"
    }

    object Staticated {
        var MY_PREFS_SHUFFLE = "Shuffle Feature"
        var MY_PREFS_LOOP = "Loop Feature"

        fun onSongComplete() {
            if (currentSongHelper?.isShuffle as Boolean) {
                playNext("PlayNextLikeNormalShuffle")
                currentSongHelper?.isPlaying = true
            } else if (currentSongHelper?.isLoop as Boolean) {
                currentSongHelper?.isPlaying = true
                var nextSong = fetchSongs?.get(currentPosition as Int)

                currentSongHelper?.currentPosition = currentPosition as Int
                currentSongHelper?.songID = nextSong?.songID as Long
                currentSongHelper?.songTitle = nextSong?.songTitle
                currentSongHelper?.songArtist = nextSong?.songArtist
                currentSongHelper?.songPath = nextSong?.songData

                updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)
                mediaPlayer?.reset()

                try {
                    mediaPlayer?.setDataSource(myActivity, Uri.parse(currentSongHelper?.songPath))
                    mediaPlayer?.prepare()
                    mediaPlayer?.start()
                    mediaPlayer?.setOnCompletionListener { onSongComplete() }
                    processInformation(mediaPlayer as MediaPlayer)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                playNext("PlayNextNormal")
                currentSongHelper?.isPlaying = true
            }

            if (favouriteContent?.checkIfIDExists(currentSongHelper?.songID?.toInt() as Int) as Boolean) {
                buttonFav?.setImageResource(R.drawable.favorite_on)
            } else {
                buttonFav?.setImageResource(R.drawable.favorite_off)
            }
        }

        fun updateTextViews(_songTitle: String, _songArtist: String) {
            var _songTitleUpdated = _songTitle
            var _songArtistUpdated = _songArtist
            if (_songTitle.equals("<unknown>", true)) {
                _songTitleUpdated = "Unknown"
            }
            if (_songArtist.equals("<unknown>", true)) {
                _songArtistUpdated = "Unknown"
            }
            songTitleText?.setText(_songTitleUpdated)
            songArtistText?.setText(_songArtistUpdated)
        }

        fun processInformation(mediaPlayer: MediaPlayer) {
            val startTime = mediaPlayer?.currentPosition
            val finalTime = mediaPlayer?.duration

            var mMinutes = (startTime?.div(1000))?.div(60)
            var mSeconds = (startTime?.div(1000))?.rem(60)
            var mEMinutes = (finalTime?.div(1000))?.div(60)
            var mESeconds = (finalTime?.div(1000))?.rem(60)

            seekBar?.max = finalTime

            startTimeText?.setText(String.format("%d:%d", mMinutes, mSeconds))
            endTimeText?.setText(String.format("%d:%d", mEMinutes, mESeconds))

            Handler().postDelayed(updateSongTime, 1000)
        }

        fun playNext(check: String) {
            if (check.equals("PlayNextNormal", true)) {
                currentPosition = currentPosition?.plus(1)
            } else if (check.equals("PlayNextLikeNormalShuffle", true)) {
                var randomObject = java.util.Random()
                var randomPosition = randomObject.nextInt(fetchSongs?.size?.plus(1) as Int)
                currentPosition = randomPosition
            }
            if (currentPosition == fetchSongs?.size) {
                currentPosition = 0
            }

            currentSongHelper?.isLoop = false
            buttonLoop?.setBackgroundResource(R.drawable.loop_white_icon)

            var nextSong = fetchSongs?.get(currentPosition as Int)
            currentSongHelper?.songID = nextSong?.songID as Long
            currentSongHelper?.songTitle = nextSong?.songTitle
            currentSongHelper?.songArtist = nextSong?.songArtist
            currentSongHelper?.songPath = nextSong?.songData
            currentSongHelper?.currentPosition = currentPosition as Int

            updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)
            mediaPlayer?.reset()

            try {
                mediaPlayer?.setDataSource(myActivity, Uri.parse(currentSongHelper?.songPath))
                mediaPlayer?.prepare()
                mediaPlayer?.start()
                mediaPlayer?.setOnCompletionListener { onSongComplete() }
                processInformation(mediaPlayer as MediaPlayer)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (favouriteContent?.checkIfIDExists(currentSongHelper?.songID?.toInt() as Int) as Boolean) {
                buttonFav?.setImageResource(R.drawable.favorite_on)
            } else {
                buttonFav?.setImageResource(R.drawable.favorite_off)
            }
        }
    }

    var mAccelaration: Float = 0f
    var mAccelarationCurrent: Float = 0f
    var mAccelarationLast: Float = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view = inflater.inflate(R.layout.fragment_song_playing, container, false)
        setHasOptionsMenu(true)
        activity?.title = "Now Playing"

        songTitleText = view?.findViewById(R.id.songTitle)
        songArtistText = view?.findViewById(R.id.songArtist)
        buttonLoop = view?.findViewById(R.id.buttonLoop)
        buttonShuffle = view?.findViewById(R.id.buttonShuffle)
        buttonNext = view?.findViewById(R.id.buttonNext)
        buttonPrevious = view?.findViewById(R.id.buttonPrevious)
        buttonPlayPause = view?.findViewById(R.id.buttonPlayPause)
        seekBar = view?.findViewById(R.id.seekBar)
        startTimeText = view?.findViewById(R.id.startTime)
        endTimeText = view?.findViewById(R.id.endTime)
        glView = view?.findViewById(R.id.visualizer_view)
        buttonFav = view?.findViewById(R.id.buttonFav)
        buttonFav?.alpha = 0.8f

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        audioVisualization = glView as AudioVisualization
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onResume() {
        super.onResume()
        audioVisualization?.onResume()
        mSensorManager?.registerListener(
            mSensorListener,
            mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        audioVisualization?.onPause()
        super.onPause()
        mSensorManager?.unregisterListener(mSensorListener)
    }

    override fun onDestroyView() {
        audioVisualization?.release()
        super.onDestroyView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSensorManager = myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelaration = 0.0f
        mAccelarationCurrent = SensorManager.GRAVITY_EARTH
        mAccelarationLast = SensorManager.GRAVITY_EARTH
        bindShakeListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item: MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = true
        val item2: MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_redirect -> {
                myActivity?.onBackPressed()
                return false
            }
        }
        return false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        favouriteContent = EchoDatabase(myActivity)
        currentSongHelper = CurrentSongHelper()
        currentSongHelper?.isShuffle = false
        currentSongHelper?.isLoop = false
        currentSongHelper?.isPlaying = true

        if (currentSongHelper?.isPlaying as Boolean) {
            Toast.makeText(myActivity, "Track Playing", Toast.LENGTH_SHORT).show()
        }

        var _path: String? = null
        var _songArtist: String? = null
        var _songTitle: String? = null
        var _songID: Long? = 0

        _path = arguments?.getString("Path")
        _songArtist = arguments?.getString("SongArtist")
        _songTitle = arguments?.getString("SongTitle")
        _songID = arguments?.getInt("SongID")?.toLong()
        currentPosition = arguments?.getInt("SongPosition")
        fetchSongs = arguments?.getParcelableArrayList("SongData")

        currentSongHelper?.songID = _songID
        currentSongHelper?.songPath = _path
        currentSongHelper?.songArtist = _songArtist
        currentSongHelper?.songTitle = _songTitle
        currentSongHelper?.currentPosition = currentPosition as Int

        updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)

        var fromMainBottomBar = arguments?.get("MainBottomBar") as? String
        var fromFavBottomBar = arguments?.get("FavBottomBar") as? String
        if (fromMainBottomBar != null) {
            mediaPlayer = MainScreenFragment.Statified.mediaPlayer
        } else if (fromFavBottomBar != null) {
            mediaPlayer = FavouritesFragment.Statified.mediaPlayer
        } else {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                mediaPlayer?.setDataSource(myActivity, Uri.parse(_path))
                mediaPlayer?.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener { onSongComplete() }
        }

        processInformation(mediaPlayer as MediaPlayer)

        if (currentSongHelper?.isPlaying as Boolean) {
            buttonPlayPause?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            buttonPlayPause?.setBackgroundResource(R.drawable.play_icon)
        }

        clickHandler()

        audioVisualization?.linkTo(mediaPlayer)

        var prefsForShuffle = myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("Feature", false)
        if (isShuffleAllowed as Boolean) {
            currentSongHelper?.isShuffle = true
            currentSongHelper?.isLoop = false
            buttonShuffle?.setBackgroundResource(R.drawable.shuffle_icon)
            buttonLoop?.setBackgroundResource(R.drawable.loop_white_icon)
        } else {
            currentSongHelper?.isShuffle = false
            buttonShuffle?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }

        var prefsForLoop = myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForLoop?.getBoolean("Feature", false)
        if (isLoopAllowed as Boolean) {
            currentSongHelper?.isShuffle = false
            currentSongHelper?.isLoop = true
            buttonShuffle?.setBackgroundResource(R.drawable.shuffle_white_icon)
            buttonLoop?.setBackgroundResource(R.drawable.loop_icon)
        } else {
            currentSongHelper?.isLoop = false
            buttonShuffle?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }

        if (favouriteContent?.checkIfIDExists(currentSongHelper?.songID?.toInt() as Int) as Boolean) {
            buttonFav?.setImageResource(R.drawable.favorite_on)
        } else {
            buttonFav?.setImageResource(R.drawable.favorite_off)
        }
    }

    fun clickHandler() {

        buttonFav?.setOnClickListener({
            if (favouriteContent?.checkIfIDExists(currentSongHelper?.songID?.toInt() as Int) as Boolean) {
                buttonFav?.setImageResource(R.drawable.favorite_off)
                favouriteContent?.deleteFavourites(currentSongHelper?.songID?.toInt() as Int)
                Toast.makeText(myActivity, "Removed from Favourites", Toast.LENGTH_SHORT).show()
            } else {
                buttonFav?.setImageResource(R.drawable.favorite_on)
                favouriteContent?.storeAsFavourite(
                    currentSongHelper?.songID?.toInt(),
                    currentSongHelper?.songPath,
                    currentSongHelper?.songArtist,
                    currentSongHelper?.songTitle
                )
                Toast.makeText(myActivity, "Added to Favourites", Toast.LENGTH_SHORT).show()
            }
        })
        buttonShuffle?.setOnClickListener({
            var editorShuffle =
                myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop = myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()
            if (currentSongHelper?.isShuffle as Boolean) {
                currentSongHelper?.isShuffle = false
                buttonShuffle?.setBackgroundResource(R.drawable.shuffle_white_icon)
                Toast.makeText(myActivity, "Shuffle Off", Toast.LENGTH_SHORT).show()
                editorShuffle?.putBoolean("Feature", false)
                editorShuffle?.apply()
            } else {
                currentSongHelper?.isShuffle = true
                currentSongHelper?.isLoop = false
                buttonShuffle?.setBackgroundResource(R.drawable.shuffle_icon)
                Toast.makeText(myActivity, "Shuffle On", Toast.LENGTH_SHORT).show()
                buttonLoop?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("Feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("Feature", false)
                editorLoop?.apply()
            }
        })
        buttonPlayPause?.setOnClickListener({
            if (mediaPlayer?.isPlaying as Boolean) {
                mediaPlayer?.pause()
                currentSongHelper?.isPlaying = false
                buttonPlayPause?.setBackgroundResource(R.drawable.play_icon)
            } else {
                mediaPlayer?.start()
                mediaPlayer?.setOnCompletionListener { onSongComplete() }
                currentSongHelper?.isPlaying = true
                buttonPlayPause?.setBackgroundResource(R.drawable.pause_icon)
            }
        })
        buttonPrevious?.setOnClickListener({
            currentSongHelper?.isPlaying = true
            if (currentSongHelper?.isLoop as Boolean) {
                buttonLoop?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()
        })
        buttonNext?.setOnClickListener({
            currentSongHelper?.isPlaying = true
            if (currentSongHelper?.isShuffle as Boolean) {
                playNext("PlayNextLikeNormalShuffle")
            } else {
                playNext("PlayNextNormal")
            }
        })
        buttonLoop?.setOnClickListener({
            var editorShuffle =
                myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop = myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()
            if (currentSongHelper?.isLoop as Boolean) {
                currentSongHelper?.isLoop = false
                buttonLoop?.setBackgroundResource(R.drawable.loop_white_icon)
                Toast.makeText(myActivity, "Loop Off", Toast.LENGTH_SHORT).show()
                editorLoop?.putBoolean("Feature", false)
                editorLoop?.apply()
            } else {
                currentSongHelper?.isLoop = true
                currentSongHelper?.isShuffle = false
                buttonLoop?.setBackgroundResource(R.drawable.loop_icon)
                Toast.makeText(myActivity, "Loop On", Toast.LENGTH_SHORT).show()
                buttonShuffle?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("Feature", false)
                editorShuffle?.apply()
                editorLoop?.putBoolean("Feature", true)
                editorLoop?.apply()
            }
        })
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    var mMinutes = (progress / 1000) / 60
                    var mSeconds = (progress / 1000) % 60
                    startTimeText?.setText(String.format("%d:%d", mMinutes, mSeconds))
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
    }

    fun playPrevious() {

        currentPosition = currentPosition?.minus(1)

        if (currentPosition == -1) {
            currentPosition = 0
        }

        if (currentSongHelper?.isPlaying as Boolean) {
            buttonPlayPause?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            buttonPlayPause?.setBackgroundResource(R.drawable.play_icon)
        }

        currentSongHelper?.isLoop = false
        buttonLoop?.setBackgroundResource(R.drawable.loop_white_icon)

        var previousSong = fetchSongs?.get(currentPosition as Int)
        currentSongHelper?.songID = previousSong?.songID as Long
        currentSongHelper?.songTitle = previousSong?.songTitle
        currentSongHelper?.songArtist = previousSong?.songArtist
        currentSongHelper?.songPath = previousSong?.songData
        currentSongHelper?.currentPosition = currentPosition as Int

        updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)
        mediaPlayer?.reset()
        try {
            mediaPlayer?.setDataSource(myActivity, Uri.parse(currentSongHelper?.songPath))
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener { onSongComplete() }
            processInformation(mediaPlayer as MediaPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (favouriteContent?.checkIfIDExists(currentSongHelper?.songID?.toInt() as Int) as Boolean) {
            buttonFav?.setImageResource(R.drawable.favorite_on)
        } else {
            buttonFav?.setImageResource(R.drawable.favorite_off)
        }
    }

    fun bindShakeListener() {
        mSensorListener = object : SensorEventListener {

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }

            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                mAccelarationLast = mAccelarationCurrent
                mAccelarationCurrent = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                val delta = mAccelarationCurrent - mAccelarationLast
                mAccelaration = (mAccelaration * 0.9f) + delta

                if (mAccelaration > 12) {
                    val prefs = myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("Feature", false)
                    if (isAllowed as Boolean) {
                        playNext("PlayNextNormal")
                    }
                }
            }
        }
    }
}