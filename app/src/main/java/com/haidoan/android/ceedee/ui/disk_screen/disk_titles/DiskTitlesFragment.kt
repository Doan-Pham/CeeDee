package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.annotation.SuppressLint

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.MenuItem.OnMenuItemClickListener
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener

import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.Genre
import com.haidoan.android.ceedee.databinding.FragmentDiskTitlesBinding
import com.haidoan.android.ceedee.utils.TypeUtils


import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_second.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class DiskTitlesFragment : Fragment() {
    private var _binding: FragmentDiskTitlesBinding? = null
    private lateinit var diskTitleAdapter: DiskTitlesAdapter
    private lateinit var diskTitlesViewModel: DiskTitlesViewModel

    private var listAllItem: ArrayList<DiskTitle> = ArrayList()
    //private var listAllItemTerm: ArrayList<DiskTitle> = ArrayList()

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

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        diskTitlesViewModel = ViewModelProvider(requireActivity())[DiskTitlesViewModel::class.java]
        diskTitleAdapter = DiskTitlesAdapter(diskTitlesViewModel, viewLifecycleOwner)
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
                    listAllItem.addAll(list)
                    diskTitleAdapter.setListData(listAllItem)
                    //listAllItemTerm.addAll(list)
                    //diskTitleAdapter.setListData(listAllItemTerm)
                    if (diskTitleAdapter.itemCount >= 2)
                        binding.tvDiskTitlesTotal.text =
                            diskTitleAdapter.itemCount.toString() + " Titles"
                    else binding.tvDiskTitlesTotal.text =
                        diskTitleAdapter.itemCount.toString() + " Title"

                    createMenu()
                    //createSearchView()
                }
                is Response.Failure -> {
                    print(response.errorMessage)
                    //Hide the ProgressBar
                    binding.progressbarDiskTitle.visibility = View.GONE
                    Log.d("TAG", "FAILURE")
                }
                else -> print(response.toString())
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
/*

    private fun createSearchView() {
        val searchView: SearchView =
            (requireActivity().toolbar.menu.findItem(R.id.menu_disk_titles_search).actionView as SearchView)
        searchView.queryHint = "Type here to search"
        searchView.maxWidth= Int.MAX_VALUE
        searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                listAllItemTerm.clear()
                val searchText = newText!!.lowercase(Locale.getDefault())
                if (searchText.isNotEmpty()){
                    listAllItemTerm.forEach{
                        if (it.name.lowercase(Locale.getDefault()).contains(searchText)){
                            listAllItemTerm.add(it)
                        }
                    }

                }
                else {
                    listAllItemTerm.clear()
                    listAllItemTerm.addAll(listAllItem)
                }

                return false
            }
        })

    }
*/

    private fun sortByCDAmount(type: TypeUtils.SORT_BY_AMOUNT) {
        diskTitleAdapter.sortByCDAmount(type)
    }

    private fun sortByName(type: TypeUtils.SORT_BY_NAME) {
        diskTitleAdapter.sortByName(type)
    }

    private fun filterByGenre(isEnabled: Boolean) {
        if (isEnabled) {
        }
        else {

        }
    }

    private fun createMenu() {
        requireActivity().toolbar.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menu.clear()
                menuInflater.inflate(R.menu.menu_disk_titles, menu)

                val searchView: SearchView =
                    (menu.findItem(R.id.menu_disk_titles_search).actionView as SearchView)
                searchView.queryHint = "Type here to search"
                searchView.maxWidth= Int.MAX_VALUE
                searchView.setOnQueryTextListener(object : OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        val listAllItemTerm = arrayListOf<DiskTitle>()
                        val searchText = newText!!.lowercase(Locale.getDefault())
                        if (searchText.isNotEmpty()){
                            listAllItem.forEach{
                                if (it.name.lowercase(Locale.getDefault()).contains(searchText)){
                                    listAllItemTerm.add(it)
                                }
                            }

                        }
                        else {
                            listAllItemTerm.clear()
                            listAllItemTerm.addAll(listAllItem)
                        }
                        diskTitleAdapter.setListData(listAllItemTerm)
                        return false
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.menu_disk_titles_cart -> {
                        Log.d("TAG_MENU", "DISKTITLE_CART")
                        true
                    }
                    R.id.menu_disk_title_tab_filter_by_genre -> {
                        filterByGenre(true)
                        true
                    }
                    R.id.menu_disk_title_tab_sort_by_name_ascending -> {
                        sortByName(TypeUtils.SORT_BY_NAME.Ascending)
                        true
                    }
                    R.id.menu_disk_title_tab_sort_by_name_descending -> {
                        sortByName(TypeUtils.SORT_BY_NAME.Descending)
                        true
                    }
                    R.id.menu_disk_title_tab_sort_by_CD_amount_ascending -> {
                        sortByCDAmount(TypeUtils.SORT_BY_AMOUNT.Ascending)
                        true
                    }
                    R.id.menu_disk_title_tab_sort_by_CD_amount_descending -> {
                        sortByCDAmount(TypeUtils.SORT_BY_AMOUNT.Descending)
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