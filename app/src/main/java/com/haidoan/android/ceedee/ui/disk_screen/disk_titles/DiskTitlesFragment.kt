package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.FragmentDiskTitlesBinding

class DiskTitlesFragment : Fragment() {

    private var _binding: FragmentDiskTitlesBinding? = null

    private val disTitleAdapter by lazy{ DiskTitlesAdapter() }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDiskTitlesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        disTitleAdapter.differ.submitList(fetchData())
        binding.apply {
            rcvDiskTitles.apply {
                layoutManager= LinearLayoutManager(activity)
                adapter=disTitleAdapter
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchData(): MutableList<DiskTitle> {
        val diskTitleList : MutableList<DiskTitle> = mutableListOf()
        diskTitleList.add(DiskTitle(1,10,"name 1","author 1","cover img 1","description 1"))
        diskTitleList.add(DiskTitle(2,20,"name 2","author 2","cover img 2","description 2"))
        diskTitleList.add(DiskTitle(3,30,"name 3","author 3","cover img 3","description 3"))
        diskTitleList.add(DiskTitle(4,40,"name 4","author 4","cover img 4","description 4"))

        return diskTitleList
    }
}