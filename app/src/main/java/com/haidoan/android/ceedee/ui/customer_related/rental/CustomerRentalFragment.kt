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
    private val filterChipIds = hashMapOf<String, Int>(
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
                filterChipIds[status.name] = View.generateViewId()
                val chip =
                    inflater.inflate(R.layout.chip_choice, binding.chipGroupFilter, false) as Chip
                chip.text = status.name
                chip.id = filterChipIds[status.name] ?: R.id.filter_chip_unknown

                if (chip.id == filterChipIds["Unknown"]) {
                    Log.e(
                        TAG,
                        "onCreateView() - viewModel.rentalStatus - Unknown rental status: ${status.name}"
                    )
                }
                binding.chipGroupFilter.addView(chip)
            }
            if (filterChipIds.values.firstOrNull() != null){
                binding.chipGroupFilter.check(filterChipIds.values.first() )
                Log.d(TAG, "viewModel.rentalStatus.observe() -: ${filterChipIds.values.first()}")
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
            viewModel.setFilteringStatus(
                filterChipIds.entries.find { it.value == group.checkedChipId }?.key ?: ""
            )
            Log.d(TAG, "CheckId change: ${group.checkedChipId}")
        }
        if (filterChipIds.values.firstOrNull() != null){
            binding.chipGroupFilter.check(filterChipIds.values.first() )
            Log.d(TAG, "setUpChipGroup() -: ${filterChipIds.values.first()}")
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
