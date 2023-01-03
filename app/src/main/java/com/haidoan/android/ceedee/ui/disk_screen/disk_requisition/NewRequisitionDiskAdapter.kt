package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.ItemNewrequisitionDiskToImportBinding

class NewRequisitionDiskAdapter(
    private val onButtonMinusClick: (DiskTitle) -> Unit,
    private val onButtonPlusClick: (DiskTitle) -> Unit,
    private val onButtonRemoveClick: (DiskTitle) -> Unit
) :
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
        private val binding: ItemNewrequisitionDiskToImportBinding,
        private val onButtonMinusClick: (DiskTitle) -> Unit,
        private val onButtonPlusClick: (DiskTitle) -> Unit,
        private val onButtonRemoveClick: (DiskTitle) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(diskTitle: DiskTitle, diskAmount: Long?) {
            binding.apply {
                imageviewDiskCover.load(diskTitle.coverImageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_disk_cover_placeholder_96)
                    error(R.drawable.ic_disk_cover_placeholder_96)
                }
                textviewDiskTitle.text = diskTitle.name
                val diskAmountString = "${diskAmount ?: 0}"
                textviewDiskAmount.text = diskAmountString

                buttonMinus.setOnClickListener { onButtonMinusClick(diskTitle) }
                buttonPlus.setOnClickListener { onButtonPlusClick(diskTitle) }
                buttonRemove.setOnClickListener { onButtonRemoveClick(diskTitle) }
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
        return NewRequisitionDiskViewHolder(
            binding,
            onButtonMinusClick,
            onButtonPlusClick,
            onButtonRemoveClick
        )
    }

    override fun onBindViewHolder(holder: NewRequisitionDiskViewHolder, position: Int) {
        holder.bind(disksToImportList[position].first, disksToImportList[position].second)
    }

    override fun getItemCount(): Int {
        return disksToImportList.size
    }
}
