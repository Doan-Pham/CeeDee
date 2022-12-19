package com.haidoan.android.ceedee.ui.rental.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.ItemNewrequisitionDiskToImportBinding

class NewRentalAdapter(
    private val onButtonMinusClick: (DiskTitle) -> Unit,
    private val onButtonPlusClick: (DiskTitle) -> Unit,
    private val onButtonRemoveClick: (DiskTitle) -> Unit
) :
    RecyclerView.Adapter<NewRentalAdapter.NewRentalViewHolder>() {

    private var disksToRentList: ArrayList<Pair<DiskTitle, Long>> = arrayListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setDisksToRent(disksToRentMap: Map<DiskTitle, Long>) {
        disksToRentList.clear()

        disksToRentMap.forEach { (diskTitle, diskAmount) ->
            disksToRentList.add(
                Pair(
                    diskTitle,
                    diskAmount
                )
            )
        }
        notifyDataSetChanged()
    }

    class NewRentalViewHolder(
        private val binding: ItemNewrequisitionDiskToImportBinding,
        private val onButtonMinusClick: (DiskTitle) -> Unit,
        private val onButtonPlusClick: (DiskTitle) -> Unit,
        private val onButtonRemoveClick: (DiskTitle) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(diskTitle: DiskTitle, diskAmount: Long?) {
            binding.apply {
                imageviewDiskCover.load(diskTitle.coverImageUrl)
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
    ): NewRentalViewHolder {
        val binding =
            ItemNewrequisitionDiskToImportBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return NewRentalViewHolder(
            binding,
            onButtonMinusClick,
            onButtonPlusClick,
            onButtonRemoveClick
        )
    }

    override fun onBindViewHolder(holder: NewRentalViewHolder, position: Int) {
        holder.bind(disksToRentList[position].first, disksToRentList[position].second)
    }

    override fun getItemCount(): Int {
        return disksToRentList.size
    }
}
