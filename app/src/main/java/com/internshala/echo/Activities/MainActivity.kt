package com.internshala.echo.Activities

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.NotificationBuilderWithBuilderAccessor
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.internshala.echo.Activities.MainActivity.Statified.builder
import com.internshala.echo.Activities.MainActivity.Statified.notificationManager
import com.internshala.echo.Adapters.NavDrawerAdapter
import com.internshala.echo.App.Staticated.CHANNEL_ID
import com.internshala.echo.Fragments.MainScreenFragment
import com.internshala.echo.Fragments.SongPlayingFragment
import com.internshala.echo.R
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    var navDrawerIcons: ArrayList<String> = arrayListOf()
    var imagesDrawerIcon = intArrayOf(
        R.drawable.navigation_allsongs,
        R.drawable.navigation_favorites,
        R.drawable.navigation_settings,
        R.drawable.navigation_aboutus
    )

    object Statified {
        var drawerLayout: DrawerLayout? = null
        var notificationManager: NotificationManagerCompat? = null
        var builder: NotificationCompat.Builder? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        navDrawerIcons.add(0, "All Songs")
        navDrawerIcons.add(1, "Favourites")
        navDrawerIcons.add(2, "Settings")
        navDrawerIcons.add(3, "About Us")

        Statified.drawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this@MainActivity, MainActivity.Statified.drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        Statified.drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()

        val mainScreenFragment = MainScreenFragment()
        this.supportFragmentManager
            .beginTransaction()
            .add(R.id.details_fragment, mainScreenFragment, "MainScreenFragment")
            .commit()

        var _navAdapter = NavDrawerAdapter(navDrawerIcons, imagesDrawerIcon, this)
        _navAdapter.notifyDataSetChanged()

        var nav_recycler_view = findViewById<RecyclerView>(R.id.nav_recycler_view)
        nav_recycler_view.layoutManager = LinearLayoutManager(this)
        nav_recycler_view.itemAnimator = DefaultItemAnimator()
        nav_recycler_view.adapter = _navAdapter
        nav_recycler_view.setHasFixedSize(true)

        Statified.notificationManager = NotificationManagerCompat.from(this)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        Statified.builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.echo_logo)
            .setContentTitle("Track playing in background")
            .setContentText("Click to go back to app")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

    }

    override fun onStart() {
        super.onStart()
        try {
            with(NotificationManagerCompat.from(this)) {
                // notificationId is a unique int for each notification that you must define
                cancel(2000)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                with(NotificationManagerCompat.from(this)) {
                    // notificationId is a unique int for each notification that you must define
                    notify(2000, Statified.builder?.build() as Notification)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            Statified.notificationManager?.cancel(2000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
