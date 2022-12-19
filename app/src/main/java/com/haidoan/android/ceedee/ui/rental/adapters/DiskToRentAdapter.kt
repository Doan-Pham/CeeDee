package com.haidoan.android.ceedee.ui.rental.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.ItemImportDiskToImportBinding

class DiskToRentAdapter(private val onDiskItemClick: (DiskTitle) -> Unit) :
    ListAdapter<DiskTitle, DiskToRentAdapter.DiskToRentViewHolder>(DiskTitleUtils()) {
    class DiskToRentViewHolder(
        private val binding: ItemImportDiskToImportBinding,
        val onDiskItemClick: (DiskTitle) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(diskTitle: DiskTitle) {
            binding.apply {
                textviewDiskTitle.text = diskTitle.name
                textviewDiskAuthor.text = diskTitle.author
                imageviewDiskCover.load(diskTitle.coverImageUrl)
                val diskAmountString = "${diskTitle.diskAmount} CD"
                textviewDiskAmount.text = diskAmountString
                linearlayoutContentWrapper.setOnClickListener { onDiskItemClick(diskTitle) }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DiskToRentViewHolder {
        val binding =
            ItemImportDiskToImportBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return DiskToRentViewHolder(binding, onDiskItemClick)
    }

    override fun onBindViewHolder(holder: DiskToRentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiskTitleUtils : DiffUtil.ItemCallback<DiskTitle>() {
        override fun areItemsTheSame(oldItem: DiskTitle, newItem: DiskTitle): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: DiskTitle, newItem: DiskTitle): Boolean {
            return oldItem.id == newItem.id
        }
    }

}