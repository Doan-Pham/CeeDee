package com.haidoan.android.ceedee.ui.rental.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.ItemImportDiskToImportBinding

enum class ShowDiskAmountOptions {
    SHOW_DISK_IN_STORE_AMOUNT,
    SHOW_DISK_AMOUNT
}

class DiskToRentAdapter(
    private val onDiskItemClick: (DiskTitle) -> Unit,
    private val showDiskAmountOption: ShowDiskAmountOptions = ShowDiskAmountOptions.SHOW_DISK_IN_STORE_AMOUNT
) :
    ListAdapter<DiskTitle, DiskToRentAdapter.DiskToRentViewHolder>(DiskTitleUtils()) {
    class DiskToRentViewHolder(
        private val binding: ItemImportDiskToImportBinding,
        val onDiskItemClick: (DiskTitle) -> Unit,
        val showDiskAmountOption: ShowDiskAmountOptions = ShowDiskAmountOptions.SHOW_DISK_IN_STORE_AMOUNT
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(diskTitle: DiskTitle) {
            binding.apply {
                textviewDiskTitle.text = diskTitle.name
                textviewDiskAuthor.text = diskTitle.author
                imageviewDiskCover.load(diskTitle.coverImageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_disk_cover_placeholder_96)
                    error(R.drawable.ic_disk_cover_placeholder_96)
                }

                var diskAmountString = "0 CD"
                if (showDiskAmountOption == ShowDiskAmountOptions.SHOW_DISK_IN_STORE_AMOUNT) {
                    diskAmountString = "${diskTitle.diskInStoreAmount} CD"
                } else if (showDiskAmountOption == ShowDiskAmountOptions.SHOW_DISK_AMOUNT) {
                    diskAmountString = "${diskTitle.diskAmount} CD"
                }
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
        return DiskToRentViewHolder(binding, onDiskItemClick, showDiskAmountOption)
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