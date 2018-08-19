package com.example.bejibx.morphingbutton

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            button.toggleStateAnimated()
        }
        buttonOutlined.setOnClickListener {
            button.switchToOutlined()
        }
        buttonFilled.setOnClickListener {
            button.switchToFilled()
        }
    }
}
