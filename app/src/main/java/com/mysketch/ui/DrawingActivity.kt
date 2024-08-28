package com.mysketch.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mysketch.R
import com.mysketch.databinding.ActivityDrawingBinding

class DrawingActivity : AppCompatActivity() {

    lateinit var binding : ActivityDrawingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDrawingBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}