package com.haidoan.android.ceedee.ui.disk_screen.disk_import

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.ItemImportDiskToImportBinding

class DiskImportAdapter :
    RecyclerView.Adapter<DiskImportAdapter.DiskRequisitionViewHolder>() {

    private var disksToImportList: ArrayList<Pair<DiskTitle, Long>> = arrayListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setDisksToImport(disksToImportMap: Map<DiskTitle, Long>) {
        disksToImportList.clear()

        disksToImportMap.forEach { (diskTitle, diskAmount) ->
            disksToImportList.add(
                Pair(
                    diskTitle,
                    diskAmount
                )
            )
        }
        notifyDataSetChanged()
    }

    class DiskRequisitionViewHolder(
        private val binding: ItemImportDiskToImportBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(diskTitle: DiskTitle, diskAmount: Long?) {
            binding.apply {
                textviewDiskTitle.text = diskTitle.name
                textviewDiskAuthor.text = diskTitle.author
                imageviewDiskCover.load(diskTitle.coverImageUrl)
                val diskAmountString = "${diskAmount ?: 0} CD"
                textviewDiskAmount.text = diskAmountString
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DiskRequisitionViewHolder {
        val binding =
            ItemImportDiskToImportBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return DiskRequisitionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiskRequisitionViewHolder, position: Int) {
        holder.bind(disksToImportList[position].first, disksToImportList[position].second)
    }

    override fun getItemCount(): Int {
        return disksToImportList.size
    }
}
