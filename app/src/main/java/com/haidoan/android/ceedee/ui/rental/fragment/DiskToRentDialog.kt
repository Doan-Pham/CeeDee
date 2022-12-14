package com.haidoan.android.ceedee.ui.rental.fragment

import android.app.Dialog
import android.os.Bundle
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.DialogChooseDiskBinding
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import com.haidoan.android.ceedee.ui.rental.adapters.DiskToRentAdapter
import com.haidoan.android.ceedee.ui.rental.viewmodel.DiskToRentViewModel
import com.haidoan.android.ceedee.ui.rental.viewmodel.NewRentalViewModel


class DiskToRentDialog(private val onDiskItemClick: (DiskTitle) -> Unit) : DialogFragment() {
    private lateinit var binding: DialogChooseDiskBinding

    private val newRentalViewModel: NewRentalViewModel by viewModels(
        ownerProducer = { requireParentFragment() })

    private val disksToRentViewModel: DiskToRentViewModel by lazy {
        ViewModelProvider(
            this, DiskToRentViewModel.Factory(
                DiskTitlesRepository(requireActivity().application),
            )
        )[DiskToRentViewModel::class.java]
    }

    private lateinit var disksToRentAdapter: DiskToRentAdapter


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogChooseDiskBinding.inflate(requireActivity().layoutInflater)

        disksToRentAdapter = DiskToRentAdapter(onDiskItemClick)
        binding.chooseDiskRecyclerView.adapter = disksToRentAdapter
        binding.chooseDiskRecyclerView.layoutManager = LinearLayoutManager(context)

        binding.search.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                disksToRentViewModel.searchDiskTitle(newText)
                return false
            }
        })

        disksToRentViewModel.diskTitlesInStore.observe(this) {
            disksToRentAdapter.submitList(it)
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setNegativeButton("Finish") { _, _ -> dialog?.cancel() }
            .create()
    }

}