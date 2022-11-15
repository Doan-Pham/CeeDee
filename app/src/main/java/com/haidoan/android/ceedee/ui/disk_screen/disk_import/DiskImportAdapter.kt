package com.haidoan.android.ceedee.ui.disk_screen.disk_import

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.haidoan.android.ceedee.databinding.ItemDiskToImportBinding

class DiskImportAdapter(private val disksToImportMap: HashMap<String, Long>) :
    RecyclerView.Adapter<DiskImportAdapter.DiskRequisitionViewHolder>() {

    private var disksToImportList: ArrayList<Pair<String, Long>> = arrayListOf()

    init {
        disksToImportMap.forEach { (diskTitle, diskAmount) ->
            disksToImportList.add(
                Pair(
                    diskTitle,
                    diskAmount
                )
            )
        }
    }


    class DiskRequisitionViewHolder(
        private val binding: ItemDiskToImportBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(diskTitle: String, diskAmount: Long?) {
            binding.apply {
                textviewDiskTitle.text = diskTitle
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
            ItemDiskToImportBinding.inflate(
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
        return disksToImportMap.size
    }
}
