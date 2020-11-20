package com.adigium.androidrfb

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private var remoteControl: RemoteControl? = null
    private var switch: Switch? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        switch = findViewById(R.id.server_switch)
        switch!!.setOnClickListener {
            if (switch!!.isChecked) {
                Log.d("MainActivity", "Server switch turned on")
                remoteControl!!.start(this, 100)
            } else {
                Log.d("MainActivity", "Server switch turned off")
                remoteControl!!.stop()
            }
        }

//        remoteControl = RemoteControl(this.applicationContext, "10.0.2.2", 60000)
        remoteControl = RemoteControl(this.applicationContext, "192.168.2.139", 60000)
        //Test logs for checking permissions
        //Only INJECT_EVENTS are required, all the other just for testing purpose
        Log.d("TESTTEST", (this.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM).toString())
        Log.d("TESTTEST", (getApplicationContext().checkCallingOrSelfPermission("android.permission.INJECT_EVENTS").toString()))
        Log.d("TESTTEST", (getApplicationContext().checkCallingOrSelfPermission("android.permission.REBOOT").toString()))
        Log.d("TESTTEST", (getApplicationContext().checkCallingOrSelfPermission("android.permission.WRITE_SETTINGS").toString()))
        Log.d("TESTTEST", (getApplicationContext().checkCallingOrSelfPermission("android.permission.BLUETOOTH_PRIVILEGED").toString()))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            remoteControl!!.onActivityResult(this, resultCode, data)
        }
    }
}