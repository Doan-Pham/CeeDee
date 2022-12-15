package com.haidoan.android.ceedee.ui.rental.fragment

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalFiresoreDataSource
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalRepository
import com.haidoan.android.ceedee.databinding.FragmentRentalBinding
import com.haidoan.android.ceedee.ui.rental.adapters.RentalSection
import com.haidoan.android.ceedee.ui.rental.viewmodel.RentalFilterCategory
import com.haidoan.android.ceedee.ui.rental.viewmodel.RentalsViewModel
import com.haidoan.android.ceedee.ui.utils.toFormattedMonthYearString
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val TAG = "RentalFragment"

class RentalFragment : Fragment() {

    private var _binding: FragmentRentalBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: RentalsViewModel by lazy {
        ViewModelProvider(
            this, RentalsViewModel.Factory(
                DiskRentalRepository(DiskRentalFiresoreDataSource())
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


//        rentalAdapter =
//            RentalAdapter(onButtonReturnClick = { rental ->
//                val action =
//                    RentalFragmentDirections.actionRentalFragmentToDiskReturnFragment(
//                        rental.id
//                    )
//                findNavController().navigate(action)
//            })
        rentalAdapter = SectionedRecyclerViewAdapter()

        binding.apply {
            fab.setOnClickListener { navigateToNewRentalFragment() }

            recyclerviewRentals.adapter = rentalAdapter
            recyclerviewRentals.layoutManager = LinearLayoutManager(activity)

            chipGroupFilter.setOnCheckedStateChangeListener { group, _ ->
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
                    R.id.chip_filter_by_all -> viewModel.setFilteringCategory(
                        RentalFilterCategory.FILTER_BY_ALL
                    )
                }
                Log.d(TAG, "CheckId change: ${group.checkedChipId}")
            }
        }

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
                    onButtonReturnClick = { rental ->
                        val action =
                            RentalFragmentDirections.actionRentalFragmentToDiskReturnFragment(
                                rental.id
                            )
                        findNavController().navigate(action)
                    })
                rentalAdapter.addSection(currentSection)
            }

            binding.recyclerviewRentals.adapter = rentalAdapter

//            rentalAdapter.submitList(
//                rentals
//            )
        }
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

    private fun navigateToNewRentalFragment() {
        val action =
            RentalFragmentDirections.actionRentalFragmentToNewRentalScreen2()
        findNavController().navigate(action)
    }
}

private fun convertToLocalDate(time: Timestamp?): String? {
    val zoneId = ZoneId.of("Asia/Ho_Chi_Minh")
    val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val localDate: LocalDate? = time?.toDate()?.toInstant()?.atZone(zoneId)?.toLocalDate()
    return dtf.format(localDate)
}