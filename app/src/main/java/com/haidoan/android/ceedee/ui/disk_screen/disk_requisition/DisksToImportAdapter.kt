package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.ItemImportDiskToImportBinding

class DisksToImportAdapter(private val onDiskItemClick: (DiskTitle) -> Unit) :
    ListAdapter<DiskTitle, DisksToImportAdapter.DisksToImportViewHolder>(DiskTitleUtils()) {

    class DisksToImportViewHolder(
        private val binding: ItemImportDiskToImportBinding,
        val onDiskItemClick: (DiskTitle) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(diskTitle: DiskTitle) {
            binding.apply {
                textviewDiskTitle.text = diskTitle.name
                textviewDiskAuthor.text = diskTitle.author
                imageviewDiskCover.load(diskTitle.coverImageUrl){
                    crossfade(true)
                    placeholder(R.drawable.ic_disk_cover_placeholder_96)
                    error(R.drawable.ic_disk_cover_placeholder_96)
                }

                val diskAmountString = "${diskTitle.diskAmount} CD"
                textviewDiskAmount.text = diskAmountString

                linearlayoutContentWrapper.setOnClickListener { onDiskItemClick(diskTitle) }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DisksToImportViewHolder {
        val binding =
            ItemImportDiskToImportBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return DisksToImportViewHolder(binding, onDiskItemClick)
    }

    override fun onBindViewHolder(holder: DisksToImportViewHolder, position: Int) {
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
