package com.mysketch.utils

import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide

// show Toast
fun AppCompatActivity.showToast(msg : String){
    Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
}
fun DialogFragment.showToast(msg : String){
    Toast.makeText(requireContext(),msg,Toast.LENGTH_SHORT).show()
}

// image load with glide
fun ImageView.loadWithGlide(drawable :Int){
    Glide.with(this)
        .load(drawable)
        .into(this)
}