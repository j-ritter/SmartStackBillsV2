package com.example.smartstackbills

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainMenu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Handling the hamburger menu click
        findViewById<ImageView>(R.id.menu_burger).setOnClickListener {
            // Open drawer or perform another action
        }

        // Handling the bell icon click
        findViewById<ImageView>(R.id.bell_icon).setOnClickListener {
            // Open notifications or perform another action
        }
    }
}