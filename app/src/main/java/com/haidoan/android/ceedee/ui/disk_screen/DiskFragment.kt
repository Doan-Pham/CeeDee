package com.haidoan.android.ceedee.ui.disk_screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.databinding.FragmentDiskBinding

class DiskFragment : Fragment() {

    private var _binding: FragmentDiskBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var diskAdapter: DiskViewPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDiskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createTabLayout()
    }

    private fun createTabLayout() {
        tabLayout = binding.tabLayout
        viewPager2 = binding.viewPager2
        diskAdapter = DiskViewPagerAdapter(this@DiskFragment)
        viewPager2.adapter = diskAdapter
        viewPager2.isUserInputEnabled = false
        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.disk_titles)
                }
                1 -> {
                    tab.text = getString(R.string.disks)
                }
                2 -> {
                    tab.text = "Requisitions"
                }
            }
        }.attach()
    }

}