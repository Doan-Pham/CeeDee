package com.haidoan.android.ceedee.ui.disk_screen

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.haidoan.android.ceedee.ui.disk_screen.disk_titles.DiskTitlesFragment
import com.haidoan.android.ceedee.ui.disk_screen.disks.DisksFragment

class DiskViewPagerAdapter (fragmentManager: FragmentManager,lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0->{
                DiskTitlesFragment()
            }
            1->{
                DisksFragment()
            }
            else->{
                Fragment()
            }
        }
    }
}