package com.mysketch.utils

import android.graphics.Bitmap
import android.os.Environment
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// show Toast
fun AppCompatActivity.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun DialogFragment.showToast(msg: String) {
    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}

// image load with glide
fun ImageView.loadWithGlide(drawable: Int) {
    Glide.with(this)
        .load(drawable)
        .into(this)
}

fun AppCompatActivity.saveImageToExternalStorage(bitmap: Bitmap): String? {

    val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

    // Get the current date and time
    val current = LocalDateTime.now()
    val fileName = "sketch" + current.format(formatter) + ".jpeg"
    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val directory = File(picturesDir, "MyAppImages") // Create a subdirectory in Pictures
    if (!directory.exists()) {
        directory.mkdirs()
    }

    val file = File(directory, fileName)
    var fos: FileOutputStream? = null
    return try {
        fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
        file.absolutePath
    } catch (e: IOException) {
        e.printStackTrace()
        null
    } finally {
        fos?.close()
    }
}