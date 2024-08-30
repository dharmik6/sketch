package com.mysketch.ui.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.mysketch.R
import com.mysketch.adapter.BrushToolAdapter
import com.mysketch.adapter.OnSelectBrush
import com.mysketch.databinding.BrushToolsBinding
import com.mysketch.ui.data.model.BrushModel
import com.mysketch.utils.showToast

class BrushTools : DialogFragment(), OnSelectBrush {
    lateinit var binding: BrushToolsBinding
    lateinit var adapter: BrushToolAdapter

    private var onDismissListener: OnDismissListener? = null

    private var onSelectBrushListener: OnSelectBrushListener? = null

    val brushList: List<BrushModel> = listOf(
        BrushModel(brushName = "Pencil", R.drawable.brush),
        BrushModel(brushName = "Pen", R.drawable.brush),
        BrushModel(brushName = "Calligraphy", R.drawable.brush),
        BrushModel(brushName = "AirBrush", R.drawable.brush),
        BrushModel(brushName = "Marker", R.drawable.brush),
        BrushModel(brushName = "HardEraser", R.drawable.brush),
        BrushModel(brushName = "SoftEraser", R.drawable.brush),
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BrushToolsBinding.inflate(layoutInflater)

        binding.rvBrushTools.layoutManager = GridLayoutManager(requireContext(), 4)
        adapter = BrushToolAdapter(requireContext(), brushList, this)
        binding.rvBrushTools.adapter = adapter
        return binding.root
    }

    override fun selectedBrushId(id: Int) {
        showToast("brush id = $id")
        onSelectBrushListener?.selectedBrushId(id)

    }

    fun setOnSelectBrushListener(onViewClickedListener: OnSelectBrushListener?) {
        this.onSelectBrushListener = onViewClickedListener
    }


    interface OnDismissListener {
        fun onDismiss()
    }

    fun setOnDismissListener(listener: OnDismissListener) {
        onDismissListener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.onDismiss()
    }

    interface OnSelectBrushListener {
        fun selectedBrushId(id: Int)
    }


}