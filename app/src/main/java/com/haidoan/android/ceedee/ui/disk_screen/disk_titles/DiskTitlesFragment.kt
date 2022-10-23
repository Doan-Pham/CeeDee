package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.annotation.SuppressLint

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuHost

import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidoan.android.ceedee.MainActivity
import com.haidoan.android.ceedee.R

import com.haidoan.android.ceedee.databinding.FragmentDiskTitlesBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_second.*


class DiskTitlesFragment : Fragment() {
    private var _binding: FragmentDiskTitlesBinding? = null
    private val diskTitleAdapter by lazy { DiskTitlesAdapter() }

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
        createMenu()
        return binding.root
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        diskTitlesViewModel = ViewModelProvider(requireActivity())[DiskTitlesViewModel::class.java]
        diskTitlesViewModel.getDiskTitles().observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {
                    //Load a ProgressBar
                    binding.progressbarDiskTitle.visibility = View.VISIBLE
                    Log.d("TAG", "LOADING...")
                }
                is Response.Success -> {
                    val postList = response.data
                    //Do what you need to do with your list
                    //Hide the ProgressBar
                    binding.progressbarDiskTitle.visibility = View.GONE
                    diskTitleAdapter.differ().submitList(postList)
                }
                is Response.Failure -> {
                    print(response.errorMessage)
                    //Hide the ProgressBar
                    binding.progressbarDiskTitle.visibility = View.GONE
                    Log.d("TAG", "FAILURE")
                }
            }
        }

        binding.apply {
            rcvDiskTitles.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = diskTitleAdapter
            }
        }
        addListeners()
    }

    private fun addListeners() {
        diskTitleAdapter.setIOnItemClickListener(object : IOnItemClickListener {
            override fun onItemClick(position: Int) {
                Log.d(
                    "TAG_ADAPTER",
                    "pos: " + position.toString() + "name: " + diskTitleAdapter.getItem(position).name
                )
            }
        })
        diskTitleAdapter.setIOnItemMoreClickListener(object : IOnItemClickListener {
            override fun onItemClick(position: Int) {
                Log.d(
                    "TAG_ADAPTER",
                    "pos: " + position.toString() + "name: " + diskTitleAdapter.getItem(position).name
                )
            }
        })
    }

    private fun createMenu() {
        requireActivity().toolbar.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menu.clear()
                menuInflater.inflate(R.menu.menu_disk_titles,menu)
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
/*
                val menu1 = menu
                    .add(Menu.NONE, 1, Menu.NONE, null)
                    .setIcon(R.drawable.ic_app_logo)
                    .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
                menu.findItem(R.id.disk_title_tab_filter_by_genre).subMenu.add(menu1)*/
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.menu_disk_titles_cart -> {
                        Log.d("TAG_MENU", "DISKTITLE_CART")
                        true
                    }
                    R.id.menu_disk_titles_filter -> {
                        Log.d("TAG_MENU", "DISKTITLE_FILTER")
                        true
                    }
                    R.id.menu_disk_titles_search -> {
                        Log.d("TAG_MENU", "DISKTITLE_SEARCH")
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}