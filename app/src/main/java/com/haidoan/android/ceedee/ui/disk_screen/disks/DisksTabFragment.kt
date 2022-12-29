package com.haidoan.android.ceedee.ui.disk_screen.disks

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.DiskStatus
import com.haidoan.android.ceedee.databinding.FragmentDiskTabDisksBinding
import com.haidoan.android.ceedee.ui.disk_screen.disk_titles.DiskTitlesViewModel
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskStatusRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response

class DisksTabFragment : Fragment() {

    private lateinit var diskAdapter: DiskAdapter
    private lateinit var diskViewModel: DiskViewModel

    private lateinit var diskTitlesViewModel: DiskTitlesViewModel
    private lateinit var diskStatusAdapter: DiskStatusAdapter

    private var _binding: FragmentDiskTabDisksBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDiskTabDisksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        createMenu()
    }

    fun init() {
        diskTitlesViewModel = ViewModelProvider(requireActivity())[DiskTitlesViewModel::class.java]
        diskViewModel = ViewModelProvider(requireActivity())[DiskViewModel::class.java]

        diskAdapter = DiskAdapter(requireActivity(), diskViewModel, viewLifecycleOwner, this)
        diskStatusAdapter = DiskStatusAdapter(requireActivity(), diskViewModel,viewLifecycleOwner,diskAdapter,binding)

        diskViewModel.getDisks().observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {
                    //Load a ProgressBar
                    binding.progressbarDisk.visibility = View.VISIBLE
                    Log.d("TAG_LIST", "LOADING...")
                }
                is Response.Success -> {
                    val list = response.data
                    //Do what you need to do with your list
                    diskAdapter.submitList(list.toMutableList())
                    diskAdapter.setAllDiskFilterByDiskStatus(list)
                    Log.d("TAG_LIST", list.toString())
                    //Hide the ProgressBar
                    binding.progressbarDisk.visibility = View.GONE
                }
                is Response.Failure -> {
                    print(response.errorMessage)
                    //Hide the ProgressBar
                    binding.progressbarDisk.visibility = View.GONE
                    Log.d("TAG_LIST", "FAILURE " + response.errorMessage)
                }
                else -> print(response.toString())
            }
        }

        diskViewModel.getDiskStatus().observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {
                    //Load a ProgressBar
                    binding.progressbarDisk.visibility = View.VISIBLE
                    Log.d("TAG", "LOADING...")
                }
                is Response.Success -> {
                    val list = response.data

                    val diskStatusList = mutableListOf<DiskStatus>()
                    diskStatusList.add(DiskStatus(DiskStatusRepository.defaultDiskStatus, "All"))
                    diskStatusList.addAll(list)
                    diskStatusAdapter.submitList(diskStatusList)

                }
                is Response.Failure -> {
                    print(response.errorMessage)
                }
                else -> print(response.toString())
            }
        }

        binding.apply {
            rcvDisk.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = diskAdapter
            }
            rcvStatus.apply {
                layoutManager = LinearLayoutManager(activity).apply {
                    orientation = LinearLayoutManager.HORIZONTAL
                }
                adapter = diskStatusAdapter
            }
            (rcvStatus.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    private fun createMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu_disk, menu)

//                val menuItemNewRequisition =
//                    menu.findItem(R.id.menu_item_disk_tab_new_requisition)

//                val newRequisitionActionView = (menuItemNewRequisition.actionView as FrameLayout)
//                val textViewNewRequisitionBadge =
//                    newRequisitionActionView.findViewById(R.id.textview_requisition_badge) as TextView
//                textViewNewRequisitionBadge.text = "10"
//
//                newRequisitionActionView.setOnClickListener {
//                    onMenuItemSelected(menuItemNewRequisition)
//                }

                val searchView: SearchView =
                    (menu.findItem(R.id.menu_disk_search).actionView as SearchView)
                searchView.queryHint = "Type here to search"
                searchView.maxWidth = Int.MAX_VALUE
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        diskAdapter.filter.filter(newText)
                        return false
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
//                    R.id.menu_item_disk_tab_new_requisition -> {
//                        Log.d("DiskTab", "Clicked Import")
//                        true
//                    }
                    R.id.menu_disk_add_genre -> {
                        addGenre()
                        true
                    }
                    R.id.menu_disk_add_disk_title -> {
                        addDiskTitle()
                        true
                    }
                    R.id.menu_disk_add_supplier -> {
                        addSupplier()
                        true
                    }
                    else -> false
                }
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }


    private fun addSupplier() {
        view?.let { withEditTextSupplier() }
    }

    private fun addDiskTitle() {
        view?.findNavController()?.navigate(R.id.diskAddEditFragment)
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