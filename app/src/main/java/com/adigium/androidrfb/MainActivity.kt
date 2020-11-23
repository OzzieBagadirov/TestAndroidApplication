package com.adigium.androidrfb

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Switch
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private var remoteControl: RemoteControl? = null
    private var toggleConnect: ToggleButton? = null
    private var proxyHost: EditText? = null
    private var proxyPort: EditText? = null
    private var downscale: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toggleConnect = findViewById(R.id.toggleButton)
        proxyHost = findViewById(R.id.proxy_host_fld)
        proxyPort = findViewById(R.id.proxy_port_fld)
        downscale = findViewById(R.id.downscale_fld)

        toggleConnect!!.setOnClickListener {
            if (toggleConnect!!.isChecked) {
                Log.d("MainActivity", "Connecting")

                val host = proxyHost!!.text.toString()
                val port = proxyPort!!.text.toString()

                remoteControl =
                    if (host != "")
                        if (port != "") RemoteControl(this.applicationContext, host, port.toInt())
                        else RemoteControl(this.applicationContext, host, 60000)
                    else if (port != "") RemoteControl(this.applicationContext, "10.0.2.2", port.toInt())
                    else RemoteControl(this.applicationContext, "10.0.2.2", 60000)

                val down = downscale!!.text.toString()
                if (down != "") remoteControl!!.downscale = down.toDouble()
                remoteControl!!.start(this, 100)
            } else {
                Log.d("MainActivity", "Disconnecting")
                remoteControl!!.stop()
            }
        }

        //Test logs for checking permissions
        //Only INJECT_EVENTS are required, all the other just for testing purpose
        Log.d("TESTTEST", (this.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM).toString())
        Log.d("TESTTEST", (getApplicationContext().checkCallingOrSelfPermission("android.permission.INJECT_EVENTS").toString()))
        Log.d("TESTTEST", (getApplicationContext().checkCallingOrSelfPermission("android.permission.REBOOT").toString()))
        Log.d("TESTTEST", (getApplicationContext().checkCallingOrSelfPermission("android.permission.WRITE_SETTINGS").toString()))
        Log.d("TESTTEST", (getApplicationContext().checkCallingOrSelfPermission("android.permission.BLUETOOTH_PRIVILEGED").toString()))
    } // First output shows that application started as system, but as you can see others are not given besides WRITE_SETTINGS
    // and let's see the issue

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            remoteControl!!.onActivityResult(this, resultCode, data)
        }
    }
}