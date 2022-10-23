package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.annotation.SuppressLint

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuHost

import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.core.view.get
import androidx.lifecycle.Lifecycle

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidoan.android.ceedee.MainActivity
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.Genre
import com.haidoan.android.ceedee.databinding.FragmentDiskTitlesBinding
import com.haidoan.android.ceedee.utils.GenreUtils
import com.haidoan.android.ceedee.utils.HashUtils


import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_second.*
import kotlinx.coroutines.runBlocking


class DiskTitlesFragment : Fragment() {
    private var _binding: FragmentDiskTitlesBinding? = null
    private val diskTitleAdapter by lazy { DiskTitlesAdapter() }

    private lateinit var diskTitlesViewModel: DiskTitlesViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var listGenre: ArrayList<Genre> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDiskTitlesBinding.inflate(inflater, container, false)
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
                    val list = response.data
                    //Do what you need to do with your list
                    //Hide the ProgressBar
                    binding.progressbarDiskTitle.visibility = View.GONE
                    diskTitleAdapter.differ().submitList(list)
                }
                is Response.Failure -> {
                    print(response.errorMessage)
                    //Hide the ProgressBar
                    binding.progressbarDiskTitle.visibility = View.GONE
                    Log.d("TAG", "FAILURE")
                }
            }
        }
        diskTitlesViewModel.getGenres().observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {
                    //Load a ProgressBar
                    Log.d("TAG", "LOADING...")
                }
                is Response.Success -> {
                    val list = response.data
                    //Do what you need to do with your list
                    //Hide the ProgressBar
                    listGenre.addAll(list)
                    createMenu()
                }
                is Response.Failure -> {
                    print(response.errorMessage)
                    //Hide the ProgressBar
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
        (requireActivity() as MainActivity).toolbar.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menu.clear()
                menuInflater.inflate(R.menu.menu_disk_titles, menu)
            }

            override fun onPrepareMenu(menu: Menu) {
                //super.onPrepareMenu(menu)
                var id = 1
                for (item in listGenre) {
                    try {
                        if (menu.findItem(id) == null) {
                            menu.findItem(R.id.disk_title_tab_filter_by_genre)
                                .subMenu!!
                                .add(Menu.NONE, id++ , Menu.NONE, item.name)
                                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
                            Log.d("TAG_ID",id.toString())
                        }

                    } catch (exception: Exception) {
                        println(exception.message)
                    }
                }
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