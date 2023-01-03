package com.haidoan.android.ceedee.ui.rental.fragment

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidoan.android.ceedee.data.Rental
import com.haidoan.android.ceedee.databinding.DialogRentalDiskTitlesBinding
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import com.haidoan.android.ceedee.ui.rental.adapters.DiskToRentAdapter
import com.haidoan.android.ceedee.ui.rental.adapters.ShowDiskAmountOptions
import com.haidoan.android.ceedee.ui.rental.viewmodel.RentalDiskTitleViewModel

class RentalDiskTitleDialog(private val currentRental: Rental) : DialogFragment() {
    companion object {
        const val TAG = "RentalDiskTitleDialog"
    }

    private lateinit var binding: DialogRentalDiskTitlesBinding

    private val disksToRentViewModel: RentalDiskTitleViewModel by lazy {
        ViewModelProvider(
            this, RentalDiskTitleViewModel.Factory(
                DiskTitlesRepository(requireActivity().application),

                )
        )[RentalDiskTitleViewModel::class.java]
    }

    private lateinit var disksToRentAdapter: DiskToRentAdapter


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogRentalDiskTitlesBinding.inflate(requireActivity().layoutInflater)

        disksToRentViewModel.setCurrentRental(currentRental)

        disksToRentAdapter = DiskToRentAdapter({}, ShowDiskAmountOptions.SHOW_DISK_AMOUNT)
        binding.recyclerviewDisksToRent.adapter = disksToRentAdapter
        binding.recyclerviewDisksToRent.layoutManager = LinearLayoutManager(context)

        disksToRentViewModel.currentRentalDiskTitles.observe(this) {
            Log.d(TAG, "currentRentalDiskTitles: $it")
            disksToRentAdapter.submitList(it)
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setNegativeButton("Finish") { _, _ -> dialog?.cancel() }
            .create()
    }
}