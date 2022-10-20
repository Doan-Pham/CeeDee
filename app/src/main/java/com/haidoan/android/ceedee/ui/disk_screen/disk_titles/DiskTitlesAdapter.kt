package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load

import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.DiskTitlesItemBinding

class DiskTitlesAdapter : RecyclerView.Adapter<DiskTitlesAdapter.DiskTitlesViewHolder>() {

    private lateinit var binding: DiskTitlesItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiskTitlesViewHolder {
        binding = DiskTitlesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiskTitlesViewHolder()
    }

    override fun onBindViewHolder(holder: DiskTitlesViewHolder, position: Int) {
        holder.setData(_differ.currentList[position])
        holder.setIsRecyclable(false)
    }

    override fun getItemCount() = _differ.currentList.size

    inner class DiskTitlesViewHolder : RecyclerView.ViewHolder(binding.root) {
        fun setData(item: DiskTitle) {
            binding.apply {
                imgDiskTitlesCoverImg.load(item.coverImageURL)
                tvDiskTitlesAmount.text = item.author
                tvDiskTitlesGenre.text = item.genreId
                tvDiskTitlesName.text = item.name
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<DiskTitle>() {
        override fun areItemsTheSame(oldItem: DiskTitle, newItem: DiskTitle): Boolean {
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: DiskTitle, newItem: DiskTitle): Boolean {
            return oldItem == newItem
        }

    }

    private val _differ = AsyncListDiffer(this, differCallback)
    fun differ(): AsyncListDiffer<DiskTitle> {
        return _differ
    }
}
