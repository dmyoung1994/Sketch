package com.codely.sketch

import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {

    var cv: CanvasView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        cv = findViewById(R.id.canvas)
    }

    fun handleVarDecButtonClick(v: View) {
        cv?.handleVarDecButtonClick()
    }

    fun handleIfElseButtonClick(v: View) {
        cv?.handleIfElseButtonClick()
    }
}
