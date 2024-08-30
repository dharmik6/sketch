package com.mysketch.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mysketch.databinding.BrushToolItemBinding
import com.mysketch.ui.data.model.BrushModel
import com.mysketch.ui.dialog.BrushTools
import com.mysketch.utils.loadWithGlide

class BrushToolAdapter(val context: Context,val list: List<BrushModel>,val listener : OnSelectBrush) : RecyclerView.Adapter<BrushToolAdapter.ViewHolder>() {
    class ViewHolder(val binding : BrushToolItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item : BrushModel,context: Context){
            binding.brushName.text = item.brushName
            if (item.brushImage != null){
                binding.brushImage.loadWithGlide(item.brushImage!!)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val binding = BrushToolItemBinding.inflate(LayoutInflater.from(context),parent,false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item,context)
        holder.binding.root.setOnClickListener{
            listener.selectedBrushId(position)
        }
    }
}

interface OnSelectBrush{
    fun selectedBrushId(id : Int)

}