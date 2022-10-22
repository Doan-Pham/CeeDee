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

    private fun destroyMenu() {
       // requireActivity().toolbar.removeMenuProvider(menuHost)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        diskTitlesViewModel = ViewModelProvider(requireActivity())[DiskTitlesViewModel::class.java]
        diskTitlesViewModel.getDiskTitles().observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {
                    //Load a ProgressBar
                    binding.progressbarDiskTitle.visibility=View.VISIBLE
                    Log.d("TAG","LOADING...")
                }
                is Response.Success -> {
                    val postList = response.data
                    //Do what you need to do with your list
                    //Hide the ProgressBar
                    binding.progressbarDiskTitle.visibility=View.GONE
                    diskTitleAdapter.differ().submitList(postList)
                }
                is Response.Failure -> {
                    print(response.errorMessage)
                    //Hide the ProgressBar
                    binding.progressbarDiskTitle.visibility=View.GONE
                    Log.d("TAG","FAILURE")
                }
            }
        }

        binding.apply {
            rcvDiskTitles.apply {
                layoutManager= LinearLayoutManager(activity)
                adapter=diskTitleAdapter
            }
        }

        addListeners()
    }

    override fun onResume() {
        super.onResume()
        addListeners()
    }

    private fun addListeners(){
        requireActivity().toolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_disk_screen_cart -> {
                    Log.d("TAG_MENU","DISKTITLE_CART")
                    true
                }
                R.id.menu_disk_screen_filter -> {
                    Log.d("TAG_MENU","DISKTITLE_FILTER")
                    true
                }
                R.id.menu_disk_screen_search -> {
                    Log.d("TAG_MENU","DISKTITLE_SEARCH")
                    true
                }
                else -> false
            }
        })
    }

    private fun createMenu() {
       requireActivity().toolbar.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu_disk_titles, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return true
                /*return when (menuItem.itemId) {
                        R.id.menu_clear -> {
                            // clearCompletedTasks()
                            true
                        }
                        R.id.menu_refresh -> {
                            // loadTasks(true)
                            true
                        }
                        else -> false
                    }*/
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        //destroyMenu()
        //toolbar.menu.add()
    }

}