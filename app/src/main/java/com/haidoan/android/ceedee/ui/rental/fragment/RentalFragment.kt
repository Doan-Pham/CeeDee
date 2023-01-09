package com.haidoan.android.ceedee.ui.rental.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.Rental
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalFirestoreDataSource
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalRepository
import com.haidoan.android.ceedee.databinding.FragmentRentalBinding
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.DisksRepository
import com.haidoan.android.ceedee.ui.rental.adapters.RentalSection
import com.haidoan.android.ceedee.ui.rental.viewmodel.RentalFilterCategory
import com.haidoan.android.ceedee.ui.rental.viewmodel.RentalsViewModel
import com.haidoan.android.ceedee.ui.utils.toFormattedMonthYearString
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter

private const val TAG = "RentalFragment"

class RentalFragment : Fragment() {

    private var _binding: FragmentRentalBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: RentalsViewModel by lazy {
        ViewModelProvider(
            this, RentalsViewModel.Factory(
                DiskRentalRepository(DiskRentalFirestoreDataSource()),
                DisksRepository(requireActivity().application),
                DiskTitlesRepository(requireActivity().application)
            )
        )[RentalsViewModel::class.java]
    }
    private lateinit var rentalAdapter: SectionedRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRentalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpOptionMenu()
        setUpRecyclerView()
        setUpFab()
        setUpChipGroup()
        observeViewModel()
    }

    private fun setUpOptionMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_rental, menu)

                val searchView: SearchView =
                    (menu.findItem(R.id.menu_item_rentals_search).actionView as SearchView)
                searchView.queryHint = "Type here to search"
                searchView.maxWidth = Int.MAX_VALUE

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.searchRental(newText)
                        return false
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_item_rentals_search -> {
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setUpRecyclerView() {
        rentalAdapter = SectionedRecyclerViewAdapter()
        binding.recyclerviewRentals.adapter = rentalAdapter
        binding.recyclerviewRentals.layoutManager = LinearLayoutManager(activity)
    }

    private fun setUpFab() {
        binding.fab.setOnClickListener { navigateToNewRentalFragment() }
    }

    private fun setUpChipGroup() {
        //TODO: Fetch rental status from backend to create chips dynamically
        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, _ ->
            when (group.checkedChipId) {
                R.id.chip_filter_by_complete -> viewModel.setFilteringCategory(
                    RentalFilterCategory.FILTER_BY_COMPLETE
                )
                R.id.chip_filter_by_in_progress -> viewModel.setFilteringCategory(
                    RentalFilterCategory.FILTER_BY_IN_PROGRESS
                )
                R.id.chip_filter_by_overdue -> viewModel.setFilteringCategory(
                    RentalFilterCategory.FILTER_BY_OVERDUE
                )
                R.id.chip_filter_by_in_request -> viewModel.setFilteringCategory(
                    RentalFilterCategory.FILTER_BY_IN_REQUEST
                )
                R.id.chip_filter_by_request_accepted -> viewModel.setFilteringCategory(
                    RentalFilterCategory.FILTER_BY_REQUEST_ACCEPTED
                )
                R.id.chip_filter_by_all -> viewModel.setFilteringCategory(
                    RentalFilterCategory.FILTER_BY_ALL
                )
            }
            Log.d(TAG, "CheckId change: ${group.checkedChipId}")
        }
    }

    private fun observeViewModel() {
        viewModel.rentals.observe(viewLifecycleOwner) { rentals ->
            val rentalsGroupedByMonth =
                rentals.sortedByDescending { it.rentDate }
                    .groupBy { it.rentDate?.toFormattedMonthYearString() }

            rentalAdapter.removeAllSections()

            for (currentMonthRentals in rentalsGroupedByMonth) {
                Log.d(TAG, "currentMonthRentals: $currentMonthRentals")
                val currentSection = RentalSection(
                    currentMonthRentals.key,
                    currentMonthRentals.value,
                    onButtonMoreClick = { rental, buttonView ->
                        setupPopupMenuForRecyclerViewItem(rental, buttonView)
                    }
                )
                rentalAdapter.addSection(currentSection)
            }

            binding.recyclerviewRentals.adapter = rentalAdapter
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun setupPopupMenuForRecyclerViewItem(rental: Rental, view: View) {
        PopupMenu(context, view).apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_item_rental_return_disk -> {
                        val action =
                            RentalFragmentDirections.actionRentalFragmentToDiskReturnFragment(
                                rental.id
                            )
                        findNavController().navigate(action)
                        true
                    }
                    R.id.menu_item_rental_accept_request -> {
                        createDialog(message = "Accept this rental made by customer: ${rental.customerName} ?") { _, _ ->
                            viewModel.acceptRentalInRequest(
                                rental
                            )
                        }
                        true
                    }
                    R.id.menu_item_rental_start_accepted_rental -> {
                        createDialog(message = "Start this rental made by customer: ${rental.customerName} ?") { _, _ ->
                            viewModel.startAcceptedRental(
                                rental
                            )
                        }
                        true
                    }
                    R.id.menu_item_rental_view_disk_titles -> {
                        RentalDiskTitleDialog(rental).show(
                            childFragmentManager,
                            "DISK_TO_ADD_DIALOG"
                        )
                        true
                    }
                    R.id.menu_item_rental_cancel -> {
                        createDialog(message = "Cancel this rental made by customer: ${rental.customerName} ?") { _, _ ->
                            viewModel.cancelRental(
                                rental
                            )
                        }
                        true
                    }
                    else -> false
                }
            }
            inflate(R.menu.popup_menu_rental_more)
            when (rental.rentalStatus) {
                "In request" -> {
                    menu.findItem(R.id.menu_item_rental_return_disk).isVisible = false
                    menu.findItem(R.id.menu_item_rental_start_accepted_rental).isVisible = false
                }
                "In progress", "Overdue" -> {
                    menu.findItem(R.id.menu_item_rental_accept_request).isVisible =
                        false
                    menu.findItem(R.id.menu_item_rental_start_accepted_rental).isVisible = false

                }
                "Complete" -> {
                    menu.findItem(R.id.menu_item_rental_return_disk).isVisible = false
                    menu.findItem(R.id.menu_item_rental_accept_request).isVisible =
                        false
                    menu.findItem(R.id.menu_item_rental_cancel).isVisible =
                        false
                    menu.findItem(R.id.menu_item_rental_start_accepted_rental).isVisible = false
                }
                "Request accepted" -> {
                    menu.findItem(R.id.menu_item_rental_return_disk).isVisible = false
                    menu.findItem(R.id.menu_item_rental_accept_request).isVisible =
                        false
                }
            }
            show()
        }
    }

    private fun navigateToNewRentalFragment() {
        val action =
            RentalFragmentDirections.actionRentalFragmentToNewRentalScreen2()
        findNavController().navigate(action)
    }

    private fun createDialog(
        title: String = "Confirmation",
        message: String,
        onPositiveButtonClick: DialogInterface.OnClickListener
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Confirm", onPositiveButtonClick)
            .setNegativeButton("Exit") { _, _ -> }
            .create()
            .show()
    }
}
