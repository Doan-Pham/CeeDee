package com.haidoan.android.ceedee.ui.report.util

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.haidoan.android.ceedee.ui.report.fragment.ReportDiskFragment
import com.haidoan.android.ceedee.ui.report.fragment.ReportFragment
import com.haidoan.android.ceedee.ui.report.fragment.RevenueExpensesFragment

class ReportViewPagerAdapter(fragment: ReportFragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> RevenueExpensesFragment()
            else -> ReportDiskFragment()
        }
    }
}