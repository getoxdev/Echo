package com.internshala.echo.Activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.widget.Toast
import com.internshala.echo.R

class SplashActivity : AppCompatActivity() {

    var permissionString = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.MODIFY_AUDIO_SETTINGS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.PROCESS_OUTGOING_CALLS,
        Manifest.permission.RECORD_AUDIO
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (!hasPermission(this@SplashActivity, *permissionString)) {
            //ask permission
            ActivityCompat.requestPermissions(this@SplashActivity, permissionString, 101)
        } else {
            showSplash()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            101 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED
                    && grantResults[3] == PackageManager.PERMISSION_GRANTED
                    && grantResults[4] == PackageManager.PERMISSION_GRANTED
                ) {
                    showSplash()
                } else {
                    Toast.makeText(this@SplashActivity, "Please grant all the permissions", Toast.LENGTH_SHORT).show()
                    this.finish()
                }
                return
            }
            else -> {
                Toast.makeText(this@SplashActivity, "Something went wrong.", Toast.LENGTH_SHORT).show()
                this.finish()
                return
            }
        }
    }

    fun hasPermission(context: Context, vararg permission: String): Boolean {
        var hasAllPermission = true
        for (member in permission) {
            val res = context.checkCallingOrSelfPermission(member)
            if (res != PackageManager.PERMISSION_GRANTED) {
                hasAllPermission = false
            }
        }
        return hasAllPermission
    }

    fun showSplash() {
        Handler().postDelayed(
            {
                val startAct = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(startAct)
                this.finish()
            }, 1000
        )
    }
}
