package com.haidoan.android.ceedee.ui.rental.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.ItemDiskToReturnBinding
import com.haidoan.android.ceedee.ui.utils.toFormattedCurrencyString

class DisksToReturnAdapter :
    ListAdapter<Triple<DiskTitle, Long, Long>, DisksToReturnAdapter.DisksToReturnViewHolder>(
        DiskToReturnUtils()
    ) {

    class DisksToReturnViewHolder(
        private val binding: ItemDiskToReturnBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(diskTitlesToReturn: Triple<DiskTitle, Long, Long>) {
            binding.apply {
                textviewDiskTitle.text = diskTitlesToReturn.first.name
                imageviewDiskCover.load(diskTitlesToReturn.first.coverImageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_disk_cover_placeholder_96)
                    error(R.drawable.ic_disk_cover_placeholder_96)
                }
                textviewDiskAmount.text = "${diskTitlesToReturn.second} CD"
                textviewDiskFee.text = diskTitlesToReturn.third.toFormattedCurrencyString()
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DisksToReturnViewHolder {
        val binding =
            ItemDiskToReturnBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return DisksToReturnViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DisksToReturnViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class DiskToReturnUtils : DiffUtil.ItemCallback<Triple<DiskTitle, Long, Long>>() {

        override fun areItemsTheSame(
            oldItem: Triple<DiskTitle, Long, Long>,
            newItem: Triple<DiskTitle, Long, Long>
        ): Boolean {
            return oldItem.first.id == newItem.first.id
        }

        override fun areContentsTheSame(
            oldItem: Triple<DiskTitle, Long, Long>,
            newItem: Triple<DiskTitle, Long, Long>
        ): Boolean {
            return oldItem.first.id == newItem.first.id && oldItem.second == newItem.second && oldItem.third == newItem.third
        }
    }

}
