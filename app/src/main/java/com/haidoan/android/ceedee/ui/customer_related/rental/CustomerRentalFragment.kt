package com.haidoan.android.ceedee.ui.customer_related.rental

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalFirestoreDataSource
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalRepository
import com.haidoan.android.ceedee.data.disk_rental.RentalStatusFirestoreDataSource
import com.haidoan.android.ceedee.data.disk_rental.RentalStatusRepository
import com.haidoan.android.ceedee.databinding.FragmentCustomerRentalBinding
import com.haidoan.android.ceedee.ui.customer_related.CustomerActivityViewModel
import com.haidoan.android.ceedee.ui.rental.adapters.RentalSection
import com.haidoan.android.ceedee.ui.utils.toFormattedMonthYearString
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter


private const val TAG = "CustomerRentalFragment"

class CustomerRentalFragment : Fragment() {

    private var _binding: FragmentCustomerRentalBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val filter_chip_ids = hashMapOf(
        "In progress" to R.id.filter_chip_in_progress,
        "In request" to R.id.filter_chip_in_request,
        "Completed" to R.id.filter_chip_complete,
        "Overdue" to R.id.filter_chip_overdue
    )
    private val viewModel: CustomerRentalViewModel by viewModels {
        CustomerRentalViewModel.Factory(
            DiskRentalRepository(DiskRentalFirestoreDataSource()), RentalStatusRepository(
                RentalStatusFirestoreDataSource()
            )
        )
    }
    private val customerActivityViewModel: CustomerActivityViewModel by activityViewModels()
    private lateinit var rentalAdapter: SectionedRecyclerViewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerRentalBinding.inflate(inflater, container, false)

        customerActivityViewModel.currentUser.observe(
            viewLifecycleOwner
        ) {
            Log.d(
                TAG,
                "customerActivityViewModel.currentUser.observe()- currentUser: ${it.phoneNumber} "
            )
            viewModel.setCurrentCustomerPhone(
                it.phoneNumber ?: "UNKNOWN_PHONE_NUMBER"
            )

        }

        viewModel.rentalStatus.observe(viewLifecycleOwner) { rentalStatus ->
            for (status in rentalStatus) {
                val chip =
                    inflater.inflate(R.layout.chip_choice, binding.chipGroupFilter, false) as Chip
                chip.text = status.name
                chip.id = filter_chip_ids[status.name] ?: R.id.filter_chip_unknown
                if (chip.id == R.id.filter_chip_unknown) {
                    Log.e(
                        TAG,
                        "onCreateView() - viewModel.rentalStatus - Unknown rental status: ${status.name}"
                    )
                }
                if (status.name == "In progress") {
                    chip.isChecked = true
                }
                binding.chipGroupFilter.addView(chip)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        setUpChipGroup()
        observeViewModel()
    }

    private fun setUpChipGroup() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, _ ->
            when (group.checkedChipId) {
                R.id.filter_chip_complete -> viewModel.setFilteringCategory(
                    RentalFilterCategory.FILTER_BY_COMPLETE
                )
                R.id.filter_chip_in_progress -> viewModel.setFilteringCategory(
                    RentalFilterCategory.FILTER_BY_IN_PROGRESS
                )
                R.id.filter_chip_overdue -> viewModel.setFilteringCategory(
                    RentalFilterCategory.FILTER_BY_OVERDUE
                )
                R.id.filter_chip_in_request -> viewModel.setFilteringCategory(
                    RentalFilterCategory.FILTER_BY_IN_REQUEST
                )
            }
            Log.d(TAG, "CheckId change: ${group.checkedChipId}")
        }
    }

    private fun setUpRecyclerView() {
        rentalAdapter = SectionedRecyclerViewAdapter()
        binding.recyclerviewRentals.adapter = rentalAdapter
        binding.recyclerviewRentals.layoutManager = LinearLayoutManager(activity)
    }

    private fun observeViewModel() {

        viewModel.rentals.observe(viewLifecycleOwner) { rentals ->
            if (rentals == null) {
                binding.progressbar.visibility = View.VISIBLE
                binding.linearlayoutContentWrapper.visibility = View.GONE
            } else {
                binding.progressbar.visibility = View.GONE
                binding.linearlayoutContentWrapper.visibility = View.VISIBLE

                val rentalsGroupedByMonth =
                    rentals.sortedByDescending { it.rentDate }
                        .groupBy { it.rentDate?.toFormattedMonthYearString() }

                rentalAdapter.removeAllSections()
                for (currentMonthRentals in rentalsGroupedByMonth) {
                    Log.d(TAG, "currentMonthRentals: $currentMonthRentals")
                    val currentSection = RentalSection(
                        currentMonthRentals.key,
                        currentMonthRentals.value,
                        onButtonMoreClick = { _, _ -> }
                    )
                    rentalAdapter.addSection(currentSection)
                }

                binding.recyclerviewRentals.adapter = rentalAdapter
            }
        }
    }
}
