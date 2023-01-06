package com.haidoan.android.ceedee.ui.customer_related.disk

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.databinding.FragmentCustomerDiskBinding
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.GenreRepository

private const val TAG = "CustomerDiskFragment"

class CustomerDiskFragment : Fragment() {

    private var _binding: FragmentCustomerDiskBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val filterChipIds = hashMapOf(
        "Unknown" to -1
    )
    private val viewModel: CustomerDiskViewModel by viewModels {
        CustomerDiskViewModel.Factory(
            DiskTitlesRepository(requireActivity().application),
            GenreRepository(requireActivity().application)
        )
    }
    private lateinit var customerDiskAdapter: CustomerDiskAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerDiskBinding.inflate(inflater, container, false)

        viewModel.genres.observe(viewLifecycleOwner) { genres ->
            //binding.chipGroupFilter.removeAllViews()
            for (genre in genres) {
                filterChipIds[genre.id] = View.generateViewId()

                val chip =
                    inflater.inflate(R.layout.chip_choice, binding.chipGroupFilter, false) as Chip
                chip.text = genre.name
                chip.id = filterChipIds[genre.id] ?: -1

                if (chip.id == filterChipIds["Unknown"]) {
                    Log.e(
                        TAG,
                        "onCreateView() - viewModel.genres.observe() - Unknown genre: ${genre.name}"
                    )
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
            viewModel.setFilteringGenreId(
                filterChipIds.entries.find { it.value == group.checkedChipId }?.key ?: ""
            )
            Log.d(TAG, "CheckId change: ${group.checkedChipId}")
            Log.d(
                TAG,
                "setUpChipGroup() - Current filtering genreId: ${filterChipIds.entries.find { it.value == group.checkedChipId }?.key}"
            )
        }
        binding.chipGroupFilter.check(filterChipIds.values.first())
    }

    private fun setUpRecyclerView() {
        customerDiskAdapter = CustomerDiskAdapter()
        binding.recyclerviewDiskTitles.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = customerDiskAdapter
            addItemDecoration(MarginItemDecoration(16))
        }
    }

    private fun observeViewModel() {
        viewModel.diskTitles.observe(viewLifecycleOwner) { diskTitles ->
            if (diskTitles == null) {
                binding.progressbar.visibility = View.VISIBLE
                binding.linearlayoutContentWrapper.visibility = View.GONE
            } else {
                binding.progressbar.visibility = View.GONE
                binding.linearlayoutContentWrapper.visibility = View.VISIBLE
                customerDiskAdapter.submitList(diskTitles)
                Log.d(TAG, "observeViewModel() - diskTitles: ${diskTitles.map { it.id }}")
            }
        }
    }


}