package com.harrysoft.burstinfowidget.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.harrysoft.burstinfowidget.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction().replace(R.id.main_fragment_container, SettingsFragment()).commit()
    }
}
