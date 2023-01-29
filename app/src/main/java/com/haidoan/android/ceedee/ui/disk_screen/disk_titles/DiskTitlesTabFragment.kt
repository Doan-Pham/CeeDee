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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.Genre
import com.haidoan.android.ceedee.databinding.FragmentDiskTabDiskTitlesBinding
import com.haidoan.android.ceedee.ui.disk_screen.repository.GenreRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import com.haidoan.android.ceedee.ui.disk_screen.utils.TypeUtils
import org.json.JSONArray
import org.json.JSONException


private const val TAG = "DiskTitlesTabFragment"

class DiskTitlesTabFragment : Fragment() {
    private var _binding: FragmentDiskTabDiskTitlesBinding? = null

    private var jsonActivityResultLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            Log.d(TAG, "Chosen json file: ${uri.toString()}")
            if (uri != null) {
                val jsonSelectedFile = context?.contentResolver?.openInputStream(uri);
                val jsonString = jsonSelectedFile?.bufferedReader().use { it?.readText() }
                Log.d(TAG, "jsonString: $jsonString")
                try {
                    val jsonArray = JSONArray(jsonString)
                    val diskTitlesToAdd = mutableListOf<DiskTitle>()
                    for (i in 0 until jsonArray.length()) {
                        Log.d(
                            TAG,
                            "jsonArray.getJSONObject(i).getString(author): ${
                                jsonArray.getJSONObject(i).getString("author")
                            }"
                        )
                        diskTitlesViewModel.addDiskTitle(
                            author = jsonArray.getJSONObject(i).getString("author"),
                            description = jsonArray.getJSONObject(i).getString("description"),
                            name = jsonArray.getJSONObject(i).getString("name"),
                            genreId = jsonArray.getJSONObject(i).getString("genreId"),
                            coverImageUrl = jsonArray.getJSONObject(i)
                                .getString("coverImageUrl")
                        ).observe(viewLifecycleOwner) {
                            if (it is Response.Success) {
                                Log.d(TAG, "Add new disk title success: ${it.data.id}")
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }

    private lateinit var diskTitleAdapter: DiskTitlesAdapter

    private lateinit
    var popularAdapter: PopularAdapter
    private lateinit var diskTitlesViewModel: DiskTitlesViewModel

    private lateinit var genreAdapter: GenreAdapter

    private var popularList = listOf<DiskTitle>()

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

    fun init() {
        diskTitlesViewModel = ViewModelProvider(requireActivity())[DiskTitlesViewModel::class.java]

        diskTitleAdapter = DiskTitlesAdapter(requireActivity())
        diskTitleAdapter.setDiskTitlesViewModel(diskTitlesViewModel)
        diskTitleAdapter.setLifecycleOwner(viewLifecycleOwner)
        diskTitleAdapter.setDiskTitlesTabFragment(this)
        genreAdapter = GenreAdapter(
            context = requireActivity().baseContext,
            diskTitlesViewModel = diskTitlesViewModel,
            viewLifecycleOwner = viewLifecycleOwner,
            diskTitlesAdapter = diskTitleAdapter,
            fragmentDiskTitlesBinding = binding
        )
        popularAdapter = PopularAdapter(requireActivity(), genreAdapter)
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
//                        binding.tvDiskTitlesTotal.text =
//                            diskTitleAdapter.itemCount.toString() + " Titles"
//                    else binding.tvDiskTitlesTotal.text =
//                        diskTitleAdapter.itemCount.toString() + " Title"

                        popularList = list.sortedByDescending { it.totalRentalAmount }.take(5)
                    popularAdapter.submitList(popularList.toMutableList())
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

                    val genreList = mutableListOf<Genre>()
                    genreList.add(Genre(GenreRepository.defaultGenre, "All"))
                    genreList.addAll(list)
                    genreAdapter.submitList(genreList)
                }
                is Response.Failure -> {
                    print(response.errorMessage)
                }
                else -> print(response.toString())
            }
        }

        val myLinearLayoutManager = object : LinearLayoutManager(requireActivity()) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }

        binding.apply {
            rcvDiskTitles.apply {
                layoutManager = myLinearLayoutManager
                adapter = diskTitleAdapter
            }
            rcvGenres.apply {
                layoutManager = LinearLayoutManager(activity).apply {
                    orientation = LinearLayoutManager.HORIZONTAL
                }
                adapter = genreAdapter
            }
            (rcvGenres.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            rcvPopular.apply {
                layoutManager = LinearLayoutManager(activity).apply {
                    orientation = LinearLayoutManager.HORIZONTAL
                }
                adapter = popularAdapter
            }
        }
    }

    private fun createMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu_disk_titles, menu)

//                val menuItemNewRequisition =
//                    menu.findItem(R.id.menu_item_disk_title_tab_new_requisition)

//                val newRequisitionActionView = (menuItemNewRequisition.actionView as FrameLayout)
//                val textViewNewRequisitionBadge =
//                    newRequisitionActionView.findViewById(R.id.textview_requisition_badge) as TextView
//                textViewNewRequisitionBadge.text = "10"
//
//                newRequisitionActionView.setOnClickListener {
//                    onMenuItemSelected(menuItemNewRequisition)
//                }

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
//                    R.id.menu_item_disk_title_tab_new_requisition -> {
//                        Log.d("DiskTitleTab", "Clicked Import")
//                        true
//                    }
                    R.id.menu_disk_titles_add_genre -> {
                        addGenre()
                        true
                    }
                    R.id.menu_disk_titles_add_disk_title -> {
                        addDiskTitle()
                        true
                    }
                    R.id.menu_disk_titles_add_disk_title_from_file -> {
                        openFile()
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
        view?.let { withEditTextSupplier() }
    }

    private fun addDiskTitle() {
        view?.findNavController()?.navigate(R.id.diskAddEditFragment)
    }


    fun openFile() {
        jsonActivityResultLauncher.launch("application/json")
    }

    private fun addGenre() {
        view?.let { withEditTextGenre() }
    }

    /**
     *  Create dialog for add genre
     * */
    private fun withEditTextGenre() {
        val builder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        builder.setTitle("Add new genre")
        val dialogLayout = inflater.inflate(R.layout.dialog_add_genre, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.edt_add_genre_name)
        builder.setView(dialogLayout)
        builder.setPositiveButton("ADD") { dialogInterface, i ->
            if (editText.text.toString() == "") {
                makeToast("Name cannot be empty!")
            } else
                addGenreToFireStore(editText.text.toString())
        }
        builder.setNegativeButton("CANCEL") { _, _ -> }
        builder.show()
    }

    private fun addGenreToFireStore(genreName: String) {
        diskTitlesViewModel.addGenres(genreName).observe(this) { response ->
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
                else -> {}
            }
        }
    }

    /**
     *  Create dialog for add supplier
     * */
    private fun withEditTextSupplier() {
        val builder = AlertDialog.Builder(context)
        val inflater = layoutInflater
        builder.setTitle("Add new supplier")
        val dialogLayout = inflater.inflate(R.layout.dialog_add_supplier, null)
        val name = dialogLayout.findViewById<EditText>(R.id.edt_add_supplier_name)
        val email = dialogLayout.findViewById<EditText>(R.id.edt_add_supplier_email)
        builder.setView(dialogLayout)
        builder.setPositiveButton("ADD") { dialogInterface, i ->

            if (email.text.toString() == "" || name.text.toString() == "") {
                makeToast("Please fill all information!")
            } else
                addSupplierToFireStore(name.text.toString(), email.text.toString())
        }
        builder.setNegativeButton("CANCEL") { _, _ -> }
        builder.show()
    }

    private fun addSupplierToFireStore(supplierName: String, supplierEmail: String) {
        val supplier = hashMapOf(
            "name" to supplierName,
            "email" to supplierEmail
        )
        diskTitlesViewModel.addSupplier(supplier).observe(this) { response ->
            when (response) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    makeToast("Add supplier success!")
                    init()
                }
                is Response.Failure -> {
                    makeToast("Add supplier fail!")
                }
                else -> {}
            }
        }
    }

    private fun makeToast(text: String) {
        Toast.makeText(requireActivity(), text, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}