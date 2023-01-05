package com.haidoan.android.ceedee.ui.customer_related.rental

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.chip.Chip
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalFirestoreDataSource
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalRepository
import com.haidoan.android.ceedee.data.disk_rental.RentalStatusFirestoreDataSource
import com.haidoan.android.ceedee.data.disk_rental.RentalStatusRepository
import com.haidoan.android.ceedee.databinding.FragmentCustomerRentalBinding
import com.haidoan.android.ceedee.ui.customer_related.CustomerActivityViewModel


private const val TAG = "CustomerRentalFragment"

class CustomerRentalFragment : Fragment() {

    private var _binding: FragmentCustomerRentalBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val filter_chip_ids = hashMapOf(
        "In progress" to R.id.filter_chip_in_progress,
        "In request" to R.id.filter_chip_in_request,
        "Completed" to R.id.filter_chip_completed,
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerRentalBinding.inflate(inflater, container, false)

        customerActivityViewModel.currentUser.observe(
            viewLifecycleOwner
        ) {
            Log.d(TAG, "customerActivityViewModel.currentUser.observe()- currentUser: ${it.phoneNumber} ")
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
                if (rentalStatus.indexOf(status) == 0) {
                    chip.isChecked = true
                }
                binding.chipGroupFilter.addView(chip)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.diskTitles.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                binding.progressbar.visibility = View.VISIBLE
                binding.linearlayoutContentWrapper.visibility = View.GONE
            } else {
                binding.progressbar.visibility = View.GONE
                binding.linearlayoutContentWrapper.visibility = View.VISIBLE
                Log.d(TAG, "viewModel.diskTitles.observe() - diskTitles: ${it.map { it.id }}")
            }
        }
    }

    private fun observeViewModel() {


    }
}
