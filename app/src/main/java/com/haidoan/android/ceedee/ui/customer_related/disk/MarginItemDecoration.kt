package com.haidoan.android.ceedee.ui.customer_related.disk

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MarginItemDecoration(private val spaceHeight: Int = 0, private val spaceWidth: Int = 0) :
    RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        with(outRect) {
            bottom = spaceHeight
            right = spaceWidth
            if (parent.getChildAdapterPosition(view) == 0) {
                top = 0
                left = 0
            } else if (parent.getChildAdapterPosition(view) == parent.childCount - 1) {
                bottom = 0
                right = 0
            }
        }
    }
}