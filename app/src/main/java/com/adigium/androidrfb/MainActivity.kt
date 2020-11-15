package com.adigium.androidrfb

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private var remoteControl: RemoteControl? = null
    var view: View? = null
    var constraint: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        view = findViewById(R.id.text_view)
        constraint = findViewById(R.id.constraint)
        view!!.setOnClickListener {
            Log.d("TESTTEST", "Clicked")
            remoteControl!!.start(this, 100)
        }
        constraint!!.setOnClickListener {
            Log.d("testtest", "root view clicked")
            remoteControl!!.stop()
        }

        remoteControl = RemoteControl(this.applicationContext)

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