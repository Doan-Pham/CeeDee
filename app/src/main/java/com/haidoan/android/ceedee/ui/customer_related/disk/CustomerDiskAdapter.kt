package com.haidoan.android.ceedee.ui.customer_related.disk

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.ItemDiskTitleBinding

class CustomerDiskAdapter(private val onButtonOptionClick: (DiskTitle, View) -> Unit) :
    ListAdapter<DiskTitle, CustomerDiskAdapter.CustomerDiskViewHolder>
        (CustomerDiskUtils()) {

    class CustomerDiskViewHolder(
        private val binding: ItemDiskTitleBinding,
        private val onButtonOptionClick: (DiskTitle, View) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(diskTitle: DiskTitle) {
            binding.apply {
                textviewDiskTitle.text = diskTitle.name
                textviewDiskAuthor.text = diskTitle.author
                textviewDiskAmount.text = "In Store: ${diskTitle.diskInStoreAmount} CD"
                imageviewDiskCover.load(diskTitle.coverImageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_disk_cover_placeholder_96)
                }
                buttonOptions.setOnClickListener {
                    onButtonOptionClick(diskTitle, buttonOptions)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CustomerDiskViewHolder {
        val binding =
            ItemDiskTitleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return CustomerDiskViewHolder(binding, onButtonOptionClick)
    }

    override fun onBindViewHolder(holder: CustomerDiskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class CustomerDiskUtils : DiffUtil.ItemCallback<DiskTitle>() {
    override fun areItemsTheSame(oldItem: DiskTitle, newItem: DiskTitle): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: DiskTitle, newItem: DiskTitle): Boolean {
        return oldItem.id == newItem.id
    }
}