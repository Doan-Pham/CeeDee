package com.haidoan.android.ceedee.ui.customer_related.rental

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidoan.android.ceedee.databinding.FragmentCustomerNewRentalBinding
import com.haidoan.android.ceedee.ui.customer_related.CustomerActivityViewModel
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import com.haidoan.android.ceedee.ui.rental.adapters.NewRentalAdapter
import com.haidoan.android.ceedee.ui.rental.fragment.DiskToRentDialog
import com.haidoan.android.ceedee.ui.rental.fragment.NewRentalScreen
import com.haidoan.android.ceedee.ui.utils.toPhoneNumberWithoutCountryCode

private const val TAG = "CustomerNewRentalFrag"

class CustomerNewRentalFragment : Fragment() {

    private var _binding: FragmentCustomerNewRentalBinding? = null
    private val binding get() = _binding!!

    private lateinit var disksToRentAdapter: NewRentalAdapter

    private val viewModel: CustomerActivityViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerNewRentalBinding.inflate(inflater, container, false)
        viewModel.updateCurrentCustomer()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        disksToRentAdapter = NewRentalAdapter(
            onButtonMinusClick = { diskTitle -> viewModel.decrementDiskTitleAmount(diskTitle) },
            onButtonPlusClick = { diskTitle -> viewModel.incrementDiskTitleAmount(diskTitle) },
            onButtonRemoveClick = { diskTitle -> viewModel.removeDiskTitleToRent(diskTitle) })
        binding.recyclerviewDisksToRent.adapter = disksToRentAdapter
        binding.recyclerviewDisksToRent.layoutManager = LinearLayoutManager(context)
        binding.buttonAddDiskToRent.setOnClickListener {
            val disksToRentDialog = DiskToRentDialog()
            disksToRentDialog.show(childFragmentManager, "DISK_TO_ADD_DIALOG")
            //viewModel.addDiskTitleToImport(DiskTitle(name = "What"))
        }
        setupButtonRequestRental()
        observeViewModel()
    }

    private fun setupButtonRequestRental() {
        binding.buttonRequestRental.setOnClickListener {
            if (disksToRentAdapter.itemCount == 0 || binding.textviewCustomerAddress.text.isEmpty() || binding.textviewCustomerName.text.isEmpty()) {
                Toast.makeText(
                    requireActivity(),
                    "You need to enter at least 1 disk title and fill all customer's information",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                viewModel.addOrUpdate(
                    binding.textviewCustomerPhone.text.toString(),
                    binding.textviewCustomerName.text.toString(),
                    binding.textviewCustomerAddress.text.toString()
                ).observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is Response.Loading -> {
                        }
                        is Response.Success -> {

                        }
                        is Response.Failure -> {
                            Log.d(NewRentalScreen.TAG, "Error: ${result.errorMessage}")
                        }
                    }
                }
//                viewModel.addRental().observe(viewLifecycleOwner) { result ->
//                    when (result) {
//                        is Response.Loading -> {
//                        }
//                        is Response.Success -> {
//                            Toast.makeText(
//                                requireActivity(),
//                                "Rental added!",
//                                Toast.LENGTH_LONG
//                            ).show()
//                            viewModel.clearDiskTitleToRent()
//
//                            if (!isCurrentUserCustomer) {
//                                findNavController().popBackStack()
//                            }
//                        }
//                        is Response.Failure -> {
//                            Log.d(NewRentalScreen.TAG, "Error: ${result.errorMessage}")
//                        }
//                    }
//                }
            }

        }

    }

    private fun observeViewModel() {
        viewModel.disksToRentAndAmount.observe(viewLifecycleOwner) {
            disksToRentAdapter.setDisksToRent(it)
            //Log.d(TAG, "disksToImport: $it")
        }
        viewModel.currentCustomer.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            binding.apply {
                textviewCustomerName.setText(it.fullName)
                textviewCustomerAddress.setText(it.address)
            }
        }
        viewModel.currentUser.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.textviewCustomerPhone.text =
                    it.phoneNumber?.toPhoneNumberWithoutCountryCode()
            }
        }
    }
}