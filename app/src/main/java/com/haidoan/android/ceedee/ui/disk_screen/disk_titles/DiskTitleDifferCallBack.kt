package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import androidx.recyclerview.widget.DiffUtil
import com.haidoan.android.ceedee.data.DiskTitle

class DiskTitleDifferCallBack(
    private val oldList: ArrayList<DiskTitle>,
    private val newList: ArrayList<DiskTitle>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id === newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].name == newList[newItemPosition].name
                && oldList[oldItemPosition].genreId == newList[newItemPosition].genreId
                && oldList[oldItemPosition].author == newList[newItemPosition].author
                && oldList[oldItemPosition].description == newList[newItemPosition].description
                && oldList[oldItemPosition].coverImageUrl == newList[newItemPosition].coverImageUrl
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return super.getChangePayload(oldItemPosition, newItemPosition)
    }
}