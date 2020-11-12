package com.adigium.androidrfb

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View

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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            remoteControl!!.onActivityResult(this, resultCode, data)
        }
    }
}