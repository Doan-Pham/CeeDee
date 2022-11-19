package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.haidoan.android.ceedee.data.Requisition
import com.haidoan.android.ceedee.databinding.ItemRequisitionBinding
import java.time.format.DateTimeFormatter

class DiskRequisitionAdapter(private val onButtonImportClick: (Requisition) -> Unit) :
    ListAdapter<Requisition, DiskRequisitionAdapter.DiskRequisitionViewHolder>
        (DiskRequisitionUtils()) {

    class DiskRequisitionViewHolder(
        private val binding: ItemRequisitionBinding,
        val onButtonImportClick: (Requisition) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(requisition: Requisition) {
            binding.apply {
                textviewSupplierName.text = requisition.supplierName

                val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                textviewRequisitionSentDate.text = dateFormatter.format(requisition.sentDate)

                if (requisition.requisitionStatus == "Completed") {
                    buttonImport.visibility = View.GONE
                } else {
                    buttonImport.setOnClickListener { onButtonImportClick(requisition) }
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DiskRequisitionViewHolder {
        val binding =
            ItemRequisitionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return DiskRequisitionViewHolder(binding, onButtonImportClick)
    }

    override fun onBindViewHolder(holder: DiskRequisitionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class DiskRequisitionUtils : DiffUtil.ItemCallback<Requisition>() {
    override fun areItemsTheSame(oldItem: Requisition, newItem: Requisition): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Requisition, newItem: Requisition): Boolean {
        return oldItem.id == newItem.id
    }
}
