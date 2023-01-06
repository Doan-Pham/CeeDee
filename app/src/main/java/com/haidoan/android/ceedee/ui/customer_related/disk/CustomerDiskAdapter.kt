package com.haidoan.android.ceedee.ui.customer_related.disk

import android.annotation.SuppressLint
import android.graphics.Rect
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

class CustomerDiskAdapter :
    ListAdapter<DiskTitle, CustomerDiskAdapter.CustomerDiskViewHolder>
        (CustomerDiskUtils()) {

    class CustomerDiskViewHolder(
        private val binding: ItemDiskTitleBinding
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
        return CustomerDiskViewHolder(binding)
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

class MarginItemDecoration(private val spaceHeight: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, view: View,
                                parent: RecyclerView, state: RecyclerView.State) {
        with(outRect) {
            if (parent.getChildAdapterPosition(view) == 0) {
                top = spaceHeight
            }
            left =  spaceHeight
            right = spaceHeight
            bottom = spaceHeight
        }
    }
}