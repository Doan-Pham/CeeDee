package com.haidoan.android.ceedee.ui.disk_screen

import androidx.fragment.app.Fragment

import androidx.viewpager2.adapter.FragmentStateAdapter
import com.haidoan.android.ceedee.ui.disk_screen.disk_requisition.DiskRequisitionsFragment
import com.haidoan.android.ceedee.ui.disk_screen.disk_titles.DiskTitlesTabFragment
import com.haidoan.android.ceedee.ui.disk_screen.disks.DisksTabFragment

class DiskViewPagerAdapter (fragment: DiskFragment): FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0->{
                DiskTitlesTabFragment()
            }
            1->{
                DisksTabFragment()
            }
            else -> {
                DiskRequisitionsFragment()
            }
        }
    }
}