package com.haidoan.android.ceedee.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.haidoan.android.ceedee.databinding.FragmentReportBinding

private val TAB_NAME_LIST = listOf("Revenue & Expenses", "Disk")

class ReportFragment : Fragment() {

    private lateinit var binding: FragmentReportBinding
    private lateinit var viewPager: ViewPager2


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReportBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPager = binding.viewPager
        viewPager.adapter = ReportViewPagerAdapter(this@ReportFragment)
        viewPager.isUserInputEnabled = false

        TabLayoutMediator(binding.tabLayout, viewPager) { tab, position ->
            tab.text = TAB_NAME_LIST[position]
        }.attach()
    }
}