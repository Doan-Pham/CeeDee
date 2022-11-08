package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.annotation.SuppressLint
import android.app.AlertDialog

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import android.widget.Toast
import androidx.core.view.MenuHost

import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle

import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.Genre
import com.haidoan.android.ceedee.databinding.FragmentDiskTabDiskTitlesBinding
import com.haidoan.android.ceedee.ui.disk_screen.repository.GenreRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response

import com.haidoan.android.ceedee.ui.disk_screen.utils.TypeUtils

import kotlinx.android.synthetic.main.activity_main.*

import java.util.*

class DiskTitlesTabFragment : Fragment() {
    private var _binding: FragmentDiskTabDiskTitlesBinding? = null

    private lateinit var diskTitleAdapter: DiskTitlesAdapter
    private lateinit var diskTitlesViewModel: DiskTitlesViewModel

    private lateinit var genreAdapter: GenreAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDiskTabDiskTitlesBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        createMenu()
    }

    private fun init() {
        diskTitlesViewModel = ViewModelProvider(requireActivity())[DiskTitlesViewModel::class.java]

        diskTitleAdapter = DiskTitlesAdapter()
        diskTitleAdapter.setDiskTitlesViewModel(diskTitlesViewModel)
        diskTitleAdapter.setLifecycleOwner(viewLifecycleOwner)
        diskTitleAdapter.setNavController(requireActivity().findNavController(R.id.mainContainer))

        genreAdapter = GenreAdapter(context = requireActivity().baseContext,
                                    diskTitlesViewModel = diskTitlesViewModel,
                                    viewLifecycleOwner = viewLifecycleOwner,
                                    diskTitlesAdapter = diskTitleAdapter,
                                    fragmentDiskTitlesBinding = binding)
        diskTitleAdapter.setGenreAdapter(genreAdapter)
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

                    diskTitleAdapter.submitList(list.toMutableList())
                    diskTitleAdapter.setAllDiskTitleFilterByGenre(list)
                    if (diskTitleAdapter.itemCount >= 2)
                        binding.tvDiskTitlesTotal.text =
                            diskTitleAdapter.itemCount.toString() + " Titles"
                    else binding.tvDiskTitlesTotal.text =
                        diskTitleAdapter.itemCount.toString() + " Title"
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
        diskTitlesViewModel.getGenres().observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {
                    //Load a ProgressBar
                    binding.progressbarDiskTitle.visibility = View.VISIBLE
                    Log.d("TAG", "LOADING...")
                }
                is Response.Success -> {
                    val list = response.data

                    val genreList= mutableListOf<Genre>()
                    genreList.add(Genre(GenreRepository.defaultGenre,"All"))
                    genreList.addAll(list)
                    genreAdapter.submitList(genreList)
                }
                is Response.Failure -> {
                    print(response.errorMessage)
                }
                else -> print(response.toString())
            }
        }

        binding.apply {
            rcvDiskTitles.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = diskTitleAdapter
            }
            rcvGenres.apply {
                layoutManager = LinearLayoutManager(activity).apply {
                    orientation = LinearLayoutManager.HORIZONTAL
                }
                adapter = genreAdapter
            }
            (rcvGenres.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    private fun createMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu_disk_titles, menu)

                val searchView: SearchView =
                    (menu.findItem(R.id.menu_disk_titles_search).actionView as SearchView)
                searchView.queryHint = "Type here to search"
                searchView.maxWidth = Int.MAX_VALUE
                searchView.setOnQueryTextListener(object : OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        diskTitleAdapter.filter.filter(newText)
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
                    R.id.menu_disk_titles_add_genre -> {
                        addGenre()
                        true
                    }
                    R.id.menu_disk_titles_add_disk_title -> {
                        addDiskTitle()
                        true
                    }
                    R.id.menu_disk_titles_add_supplier -> {
                        addSupplier()
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


    private fun sortByCDAmount(type: TypeUtils.SORT_BY_AMOUNT) {
        diskTitleAdapter.sortByCDAmount(type)
    }

    private fun sortByName(type: TypeUtils.SORT_BY_NAME) {
        diskTitleAdapter.sortByName(type)
    }

    private fun addSupplier() {
        TODO("Not yet implemented")
    }

    private fun addDiskTitle() {
        view?.findNavController()?.navigate(R.id.diskAddEditFragment)
    }

    private fun addGenre() {
        view?.let { withEditTextGenre(it) }
    }

    /**
     *  Create dialog for add genre
     * */
    private fun withEditTextGenre(view: View) {
        val builder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        builder.setTitle("Add new genre")
        val dialogLayout = inflater.inflate(R.layout.dialog_add_genre, null)
        val editText  = dialogLayout.findViewById<EditText>(R.id.editText)
        builder.setView(dialogLayout)
        builder.setPositiveButton("ADD") { dialogInterface, i -> addGenreToFireStore(editText.text.toString()) }
        builder.setNegativeButton("CANCEL") {dialogLayout,i -> }
        builder.show()
    }

    private fun addGenreToFireStore(genreName: String) {
        val genre = hashMapOf("name" to genreName)
        diskTitlesViewModel.addGenres(genre).observe(this) { response ->
            when (response) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    makeToast("Add genre success!")
                    init()
                }
                is Response.Failure -> {
                    makeToast("Add genre fail!")
                }
            }

        }
    }

    private fun makeToast(text: String) {
        Toast.makeText(context,  text, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}