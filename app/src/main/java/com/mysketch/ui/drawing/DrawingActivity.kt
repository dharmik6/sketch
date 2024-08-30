package com.mysketch.ui.drawing

import android.Manifest
import android.animation.Animator
import android.animation.ValueAnimator
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore.Images
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.mysketch.R
import com.mysketch.base.BaseActivity
import com.mysketch.databinding.ActivityDrawingBinding
import com.mysketch.databinding.BrushSettingsBinding
import com.mysketch.databinding.MenuPopupBinding
import com.mysketch.databinding.SaveDialogBinding
import com.mysketch.ui.dialog.BrushTools
import com.mysketch.utils.AppConstant
import com.mysketch.utils.saveImageToExternalStorage
import com.mysketch.utils.showToast
import com.raed.rasmview.RasmContext
import com.raed.rasmview.brushtool.data.Brush
import com.raed.rasmview.brushtool.data.BrushesRepository
import com.raed.rasmview.brushtool.model.BrushConfig
import com.raed.rasmview.state.RasmState
import java.io.File
import java.io.OutputStream


class DrawingActivity : BaseActivity(), OnClickListener, BrushTools.OnSelectBrushListener {

    lateinit var binding: ActivityDrawingBinding
    lateinit var popBinding: BrushSettingsBinding
    lateinit var brushDialog: BrushTools

    lateinit var popupWindow: PopupWindow
    lateinit var menuPopupWindow: PopupWindow
    lateinit var popupView: View
    lateinit var menuPopupView: View

    lateinit var rasmState: RasmState
    lateinit var brushConfig: BrushConfig
    lateinit var rasmContext: RasmContext
    lateinit var brushesRepository: BrushesRepository

    private val TAG = "DrawingActivity"

    private var isBoardSelected = false

    var mDefaultColor: Int = 0
    var mBrushSize: Int = 20
    var mFlow: Int = 100
    var mOpacity: Int = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDrawingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        brushDialog = BrushTools()

        mDefaultColor = ContextCompat.getColor(this, R.color.black)
        binding.rasmView.height
        rasmContext = binding.rasmView.rasmContext
        brushesRepository = BrushesRepository(resources)
        rasmState = rasmContext.state
        rasmContext.brushConfig = brushesRepository.get(Brush.Pencil)
        rasmContext.brushColor = mDefaultColor
        rasmContext.rotationEnabled = true
        brushConfig = rasmContext.brushConfig
        binding.colorCard.setCardBackgroundColor(mDefaultColor)

        val inflater = LayoutInflater.from(this)
        popupView = inflater.inflate(R.layout.brush_settings, null)
        popBinding = BrushSettingsBinding.bind(popupView)
        setBrushSeekBar()
        brush()
        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        menuPopupView = LayoutInflater.from(this).inflate(R.layout.menu_popup, null)
        menuPopupWindow = PopupWindow(
            menuPopupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )


        brushDialog.setOnDismissListener(object : BrushTools.OnDismissListener {
            override fun onDismiss() {
                // Change the brush tool tint color to gray when the dialog is dismissed
                binding.brushTools.setColorFilter(
                    ContextCompat.getColor(this@DrawingActivity, R.color.light_grey),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
        })

        popupWindow.setOnDismissListener {
            binding.brushSettings.setColorFilter(
                ContextCompat.getColor(this@DrawingActivity, R.color.light_grey),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }

        menuPopupWindow.setOnDismissListener {
            binding.menu.setColorFilter(
                ContextCompat.getColor(this@DrawingActivity, R.color.light_grey),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }

        setClicks()

    }

    fun setClicks() {
        binding.colorPicker.setOnClickListener(this)
        binding.brushSettings.setOnClickListener(this)
        binding.brushTools.setOnClickListener(this)
        binding.undo.setOnClickListener(this)
        binding.redo.setOnClickListener(this)
        binding.board.setOnClickListener(this)
        binding.menu.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.colorPicker -> {
                colorDialog()
            }

            binding.brushSettings -> {
                showCustomPopup(binding.brushSettings)
            }

            binding.undo -> {
                if (rasmState.canCallUndo()) {
                    rasmState.undo()
                }
            }

            binding.redo -> {
                if (rasmState.canCallRedo()) {
                    rasmState.redo()
                }
            }

            binding.board -> {
                toggleIcons()
            }

            binding.menu -> {
                showMenuPopup(binding.menu)
            }

            binding.brushTools -> {
                brushDialog.setOnSelectBrushListener(this)
                brushDialog.show(supportFragmentManager, brushDialog.tag)

                binding.brushTools.setColorFilter(
                    ContextCompat.getColor(this, R.color.black),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }
        }
    }

    private fun brush() {
        popBinding.sizeSeekBar.max = 100
        popBinding.sizeSeekBar.min = 10
        popBinding.sizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mBrushSize = progress
                setBrushSeekBar()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        popBinding.flowSeekBar.max = 100
        popBinding.flowSeekBar.min = 1
        popBinding.flowSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mFlow = progress
                setBrushSeekBar()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        popBinding.opacitySeekBar.max = 100
        popBinding.opacitySeekBar.min = 1
        popBinding.opacitySeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                mOpacity = progress
                setBrushSeekBar()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
    }

    private fun colorDialog() {
        ColorPickerDialog
            .Builder(this)
            .setTitle("select color")
            .setColorShape(ColorShape.SQAURE)
            .setDefaultColor(mDefaultColor)
            .setColorListener { color, colorHex ->
                showToast("color = $color")
                rasmContext.brushColor = color
                binding.colorCard.setCardBackgroundColor(color)
                mDefaultColor = color
            }
            .show()
    }

    private fun showCustomPopup(anchorView: View) {
        popupWindow.elevation = 2f

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWidth = popupView.measuredWidth

        val offsetX = (anchorView.width - popupWidth) / 2
        val offsetY = -anchorView.height - popupView.measuredHeight - 15

        popupWindow.showAsDropDown(anchorView, offsetX, offsetY)
        binding.brushSettings.setColorFilter(
            ContextCompat.getColor(this@DrawingActivity, R.color.black),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }

    override fun selectedBrushId(id: Int) {
        showToast("activity brush id = $id")
        brushDialog.dismiss()
        setBrush(id)
    }

    private fun setBrush(id: Int) {
        when (id) {
            0 -> {
                brushConfig = brushesRepository.get(Brush.Pencil)
                rasmContext.brushConfig = brushConfig
                setBrushSeekBar()
            }

            1 -> {
                brushConfig = brushesRepository.get(Brush.Pen)
                rasmContext.brushConfig = brushConfig
                setBrushSeekBar()
            }

            2 -> {
                brushConfig = brushesRepository.get(Brush.Calligraphy)
                rasmContext.brushConfig = brushConfig
                setBrushSeekBar()
            }

            3 -> {
                brushConfig = brushesRepository.get(Brush.AirBrush)
                rasmContext.brushConfig = brushConfig
                setBrushSeekBar()
            }

            4 -> {
                brushConfig = brushesRepository.get(Brush.Marker)
                rasmContext.brushConfig = brushConfig
                setBrushSeekBar()
            }

            5 -> {
                brushConfig = brushesRepository.get(Brush.HardEraser)
                rasmContext.brushConfig = brushConfig
                rasmContext.brushConfig.isEraser = true
                setBrushSeekBar()
            }

            6 -> {
                brushConfig = brushesRepository.get(Brush.SoftEraser)
                rasmContext.brushConfig = brushConfig
                setBrushSeekBar()
            }
        }
    }

    private fun setBrushSeekBar() {
        popBinding.sizeSeekBar.progress = mBrushSize
        brushConfig.size = mBrushSize / 100f

        popBinding.flowSeekBar.progress = mFlow
        brushConfig.flow = mFlow / 100f

        popBinding.opacitySeekBar.progress = mOpacity
        brushConfig.opacity = mOpacity / 100f
    }

    private fun toggleIcons() {
        // Get the width of the board icon and the full container
        val boardIconWidth =
            binding.board.width + (binding.board.paddingStart + binding.board.paddingEnd)
        val fullWidth = binding.iconContainer.width

        val targetWidth = if (isBoardSelected) fullWidth else boardIconWidth

        val animator = ValueAnimator.ofInt(binding.cardView.width, targetWidth)

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                // Before animation starts
            }

            override fun onAnimationEnd(animation: Animator) {
                if (isBoardSelected) {
                    // show icons
                    for (i in 0 until binding.iconContainer.childCount) {
                        val child = binding.iconContainer.getChildAt(i)
                        if (child.id != R.id.board) {
                            child.visibility = View.VISIBLE
                        }
                    }
                    binding.board.setColorFilter(
                        ContextCompat.getColor(this@DrawingActivity, R.color.light_grey),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    binding.board.setImageDrawable(getDrawable(R.drawable.maximize))

                } else {
                    // hide icons
                    for (i in 0 until binding.iconContainer.childCount) {
                        val child = binding.iconContainer.getChildAt(i)
                        if (child.id != R.id.board) {
                            child.visibility = View.GONE
                        }
                    }

                    binding.board.setColorFilter(
                        ContextCompat.getColor(this@DrawingActivity, R.color.black),
                        android.graphics.PorterDuff.Mode.SRC_IN
                    )
                    binding.board.setImageDrawable(getDrawable(R.drawable.fullscreen))

                }
                isBoardSelected = !isBoardSelected
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        animator.start()
    }


    private fun showMenuPopup(anchorView: View) {
        val menuPopBinding = MenuPopupBinding.bind(menuPopupView)



        menuPopupWindow.elevation = 2f

        menuPopupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        menuPopBinding.newSketch.setOnClickListener {
            newSketch()
        }
        menuPopBinding.save.setOnClickListener {
            if (checkPermission())
                save()
        }
        menuPopBinding.share.setOnClickListener {
            share()
        }
        val offsetX = 0
        val offsetY = -anchorView.height - menuPopupView.measuredHeight - 15

        menuPopupWindow.showAsDropDown(anchorView, offsetX, offsetY)
        binding.menu.setColorFilter(
            ContextCompat.getColor(this@DrawingActivity, R.color.black),
            android.graphics.PorterDuff.Mode.SRC_IN
        )
    }

    private fun newSketch() {
        rasmContext.clear()
    }

    private fun save() {
        val bitmap = rasmContext.exportRasm() // Your bitmap here
        val savedImagePath = saveImageToExternalStorage(bitmap)

        if (savedImagePath != null) {
            showToast("Sketch saved")

            // Notify media scanner
            val file = File(savedImagePath)
            val uri = Uri.fromFile(file)
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            intent.data = uri
            sendBroadcast(intent)
        } else {
            Log.e(TAG, "save: image not saved")
        }
    }

    private fun share() {
        val icon: Bitmap = rasmContext.exportRasm()
        val share = Intent(Intent.ACTION_SEND)
        share.setType("image/jpeg")

        val values = ContentValues()
        values.put(Images.Media.TITLE, "title")
        values.put(Images.Media.MIME_TYPE, "image/jpeg")
        val uri = contentResolver.insert(
            Images.Media.EXTERNAL_CONTENT_URI,
            values
        )
        val outstream: OutputStream?
        try {
            outstream = contentResolver.openOutputStream(uri!!)
            icon.compress(Bitmap.CompressFormat.JPEG, 100, outstream!!)
            outstream.close()
        } catch (e: Exception) {
            System.err.println(e.toString())
        }

        share.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(share, "Share Image"))
    }

    private fun preference() {

    }


    override fun onBackPressed() {


        val dialogBinding = SaveDialogBinding.inflate(LayoutInflater.from(this))

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        alertDialog.show()

        dialogBinding.exit.setOnClickListener {
            alertDialog.dismiss()
            super.onBackPressed()
        }

        dialogBinding.saveAndExit.setOnClickListener {
            // add save method
            alertDialog.dismiss()
            super.onBackPressed()
        }

        dialogBinding.cancel.setOnClickListener {
            alertDialog.dismiss()
        }

    }


    fun checkPermission(): Boolean {

        return if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED && ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), AppConstant.GALLERY_INTENT_REQUEST_CODE
            )
            return false
        } else {
            return true
        }
    }
}
