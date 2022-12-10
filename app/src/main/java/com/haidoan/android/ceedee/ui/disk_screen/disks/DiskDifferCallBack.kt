package com.haidoan.android.ceedee.ui.disk_screen.disks

import androidx.recyclerview.widget.DiffUtil
import com.haidoan.android.ceedee.data.Disk

class DiskDifferCallBack(
    private val oldList: List<Disk>,
    private val newList: List<Disk>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id === newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].diskTitleId == newList[newItemPosition].diskTitleId
                && oldList[oldItemPosition].currentRentalId == newList[newItemPosition].currentRentalId
                && oldList[oldItemPosition].status == newList[newItemPosition].status
                && oldList[oldItemPosition].importDate == newList[newItemPosition].importDate
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}