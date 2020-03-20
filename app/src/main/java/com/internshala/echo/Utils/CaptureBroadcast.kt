package com.internshala.echo.Utils

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationManagerCompat
import android.telephony.TelephonyManager
import com.internshala.echo.Activities.MainActivity
import com.internshala.echo.Fragments.SongPlayingFragment
import com.internshala.echo.R
import java.lang.Exception

class CaptureBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_NEW_OUTGOING_CALL) {
            try {
                with(NotificationManagerCompat.from(context as Context)) {
                    // notificationId is a unique int for each notification that you must define
                    cancel(2000)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                    SongPlayingFragment.Statified.mediaPlayer?.pause()
                    SongPlayingFragment.Statified.buttonPlayPause?.setBackgroundResource(R.drawable.play_icon)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val tm: TelephonyManager = context?.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
            when (tm?.callState) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    try {
                        with(NotificationManagerCompat.from(context as Context)) {
                            // notificationId is a unique int for each notification that you must define
                            cancel(2000)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    try {
                        if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean) {
                            SongPlayingFragment.Statified.mediaPlayer?.pause()
                            SongPlayingFragment.Statified.buttonPlayPause?.setBackgroundResource(R.drawable.play_icon)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                else -> {

                }
            }
        }
    }
}