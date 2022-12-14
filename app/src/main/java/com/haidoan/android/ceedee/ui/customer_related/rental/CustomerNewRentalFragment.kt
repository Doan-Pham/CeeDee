package com.haidoan.android.ceedee.ui.customer_related.rental

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
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
            val disksToRentDialog =
                DiskToRentDialog { diskTitle -> viewModel.addDiskTitleToRent(diskTitle) }
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
                val customerPhone = binding.textviewCustomerPhone.text.toString()
                val customerName = binding.textviewCustomerName.text.toString()
                val customerAddress = binding.textviewCustomerAddress.text.toString()
                createDialog(message = "Send a new rental request under this name: $customerName ?") { _, _ ->
                    viewModel.addOrUpdateCustomerInfo(
                        customerPhone,
                        customerName,
                        customerAddress
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

                    viewModel.requestRental(
                        customerPhone,
                        customerName,
                        customerAddress
                    ).observe(viewLifecycleOwner) { result ->
                        when (result) {
                            is Response.Loading -> {
                                binding.progressbar.visibility = View.VISIBLE
                                binding.linearlayoutContentWrapper.visibility = View.GONE
                                binding.buttonRequestRental.visibility = View.GONE
                            }
                            is Response.Success -> {
                                Toast.makeText(
                                    requireActivity(),
                                    "New rental request sent!",
                                    Toast.LENGTH_LONG
                                ).show()
                                viewModel.clearDiskTitleToRent()
                                findNavController().popBackStack()
                            }
                            is Response.Failure -> {
                                Log.d(NewRentalScreen.TAG, "Error: ${result.errorMessage}")
                            }
                        }
                    }
                }

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

    private fun createDialog(
        title: String = "Confirmation",
        message: String,
        onPositiveButtonClick: DialogInterface.OnClickListener
    ) {
        // 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor

        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Request", onPositiveButtonClick)
            .setNegativeButton("Cancel") { _, _ -> }
            .create()
            .show()
    }
}