package com.haidoan.android.ceedee.ui.customer_related.disk

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.ItemCustomerPopularDiskTitleBinding

class CustomerPopularDiskAdapter :
    ListAdapter<DiskTitle, CustomerPopularDiskAdapter.CustomerPopularDiskViewHolder>
        (CustomerDiskUtils()) {

    class CustomerPopularDiskViewHolder(
        private val binding: ItemCustomerPopularDiskTitleBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(diskTitle: DiskTitle) {
            binding.apply {
                textviewDiskTitle.text = diskTitle.name
                textviewDiskAuthor.text = diskTitle.author
                imageviewDiskCover.load(diskTitle.coverImageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_disk_cover_placeholder_96)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CustomerPopularDiskViewHolder {
        val binding =
            ItemCustomerPopularDiskTitleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return CustomerPopularDiskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerPopularDiskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class CustomerDiskUtils : DiffUtil.ItemCallback<DiskTitle>() {
        override fun areItemsTheSame(oldItem: DiskTitle, newItem: DiskTitle): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: DiskTitle, newItem: DiskTitle): Boolean {
            return oldItem.id == newItem.id
        }
    }
}

