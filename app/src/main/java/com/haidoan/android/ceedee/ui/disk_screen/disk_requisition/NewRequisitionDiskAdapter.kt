package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.ItemNewrequisitionDiskToImportBinding

class NewRequisitionDiskAdapter :
    RecyclerView.Adapter<NewRequisitionDiskAdapter.NewRequisitionDiskViewHolder>() {

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

    class NewRequisitionDiskViewHolder(
        private val binding: ItemNewrequisitionDiskToImportBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(diskTitle: DiskTitle, diskAmount: Long?) {
            binding.apply {
                textviewDiskTitle.text = diskTitle.name
                val diskAmountString = "${diskAmount ?: 0}"
                textviewDiskAmount.text = diskAmountString
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NewRequisitionDiskViewHolder {
        val binding =
            ItemNewrequisitionDiskToImportBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return NewRequisitionDiskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewRequisitionDiskViewHolder, position: Int) {
        holder.bind(disksToImportList[position].first, disksToImportList[position].second)
    }

    override fun getItemCount(): Int {
        return disksToImportList.size
    }
}
