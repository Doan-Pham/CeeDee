package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.annotation.SuppressLint

import android.os.Bundle
import android.util.Log

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager


import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.FragmentDiskTitlesBinding


class DiskTitlesFragment : Fragment() {
    private var _binding: FragmentDiskTitlesBinding? = null
    private val diskTitleAdapter by lazy{ DiskTitlesAdapter() }

    private lateinit var diskTitlesViewModel: DiskTitlesViewModel
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

        diskTitlesViewModel = ViewModelProvider(requireActivity())[DiskTitlesViewModel::class.java]
        diskTitlesViewModel.getDiskTitles().observe(this) { response ->
            when (response) {
                is Response.Loading -> {
                    //Load a ProgessBar
                    Log.d("TAG","LOADING...")
                }
                is Response.Success -> {
                    val postList = response.data
                    //Do what you need to do with your list
                    //Hide the ProgessBar
                    diskTitleAdapter.differ().submitList(postList)
                }
                is Response.Failure -> {
                    print(response.errorMessage)
                    //Hide the ProgessBar
                    Log.d("TAG","FAILURE")
                }
            }
        }


    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            rcvDiskTitles.apply {
                layoutManager= LinearLayoutManager(activity)
                adapter=diskTitleAdapter
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}