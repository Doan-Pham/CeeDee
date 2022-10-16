package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.core.View
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.DiskTitlesItemBinding

class DiskTitlesAdapter :RecyclerView.Adapter<DiskTitlesAdapter.DiskTitlesViewHolder>() {

    private lateinit var binding: DiskTitlesItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiskTitlesViewHolder {
        binding= DiskTitlesItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return DiskTitlesViewHolder()
    }

    override fun onBindViewHolder(holder: DiskTitlesViewHolder, position: Int) {
        holder.setData(differ.currentList[position])
        holder.setIsRecyclable(false)
    }

    override fun getItemCount()=differ.currentList.size

    inner class DiskTitlesViewHolder : RecyclerView.ViewHolder(binding.root){
        fun setData(item : DiskTitle){
            binding.apply {
                tvDiskTitlesAmount.text=item.author.toString()
                tvDiskTitlesGenre.text=item.genreId.toString()
                tvDiskTitlesName.text=item.name.toString()
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<DiskTitle>(){
        override fun areItemsTheSame(oldItem: DiskTitle, newItem: DiskTitle): Boolean {
            return  oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: DiskTitle, newItem: DiskTitle): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this,differCallback)
}
