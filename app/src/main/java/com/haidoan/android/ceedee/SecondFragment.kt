package com.haidoan.android.ceedee

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.haidoan.android.ceedee.databinding.FragmentSecondBinding
import com.haidoan.android.ceedee.ui.disk_screen.DiskViewPagerAdapter

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

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

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /*      binding.buttonSecond.setOnClickListener {
                  findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
              }*/

        tabLayout = binding.tabLayout
        viewPager2 = binding.viewPager2

        diskAdapter = DiskViewPagerAdapter(parentFragmentManager, lifecycle)

        viewPager2.adapter = diskAdapter

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = getString(R.string.disk_titles)
                }
                1 -> {
                    tab.text = getString(R.string.disks)
                }
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}