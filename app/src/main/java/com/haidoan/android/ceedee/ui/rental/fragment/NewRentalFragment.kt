package com.haidoan.android.ceedee.ui.rental.fragment


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidoan.android.ceedee.data.customer.Customer
import com.haidoan.android.ceedee.data.customer.CustomerFireStoreDataSource
import com.haidoan.android.ceedee.data.customer.CustomerRepository
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalFirestoreDataSource
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalRepository
import com.haidoan.android.ceedee.databinding.FragmentNewRentalScreenBinding
import com.haidoan.android.ceedee.ui.disk_screen.repository.DisksRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import com.haidoan.android.ceedee.ui.rental.adapters.NewRentalAdapter
import com.haidoan.android.ceedee.ui.rental.viewmodel.NewRentalViewModel


class NewRentalScreen : Fragment() {
    private var _binding: FragmentNewRentalScreenBinding? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var isCurrentUserCustomer: Boolean = false


    private val viewModel: NewRentalViewModel by viewModels(
        factoryProducer = {
            NewRentalViewModel.Factory(
                DiskRentalRepository(DiskRentalFirestoreDataSource()),
                DisksRepository(requireActivity().application),
                CustomerRepository(CustomerFireStoreDataSource())
            )
        })
    private lateinit var disksToRentAdapter: NewRentalAdapter

    private val customers = mutableListOf<Customer>()
    private var selectedCustomerId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewRentalScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isCurrentUserCustomer = arguments?.getBoolean(ARGUMENT_KEY_IS_USER_CUSTOMER) ?: false
        Log.d(TAG, "OnViewCreated() - isCurrentUserCustomer: $isCurrentUserCustomer")
        viewModel.setIsCurrentUserCustomer(isCurrentUserCustomer)

        disksToRentAdapter = NewRentalAdapter(
            onButtonMinusClick = { diskTitle -> viewModel.decrementDiskTitleAmount(diskTitle) },
            onButtonPlusClick = { diskTitle -> viewModel.incrementDiskTitleAmount(diskTitle) },
            onButtonRemoveClick = { diskTitle -> viewModel.removeDiskTitleToImport(diskTitle) })
        binding.newRentalRcl.adapter = disksToRentAdapter
        binding.newRentalRcl.layoutManager = LinearLayoutManager(context)
        binding.openDialog.setOnClickListener {
            val disksToRentDialog = DiskToRentDialog()
            disksToRentDialog.show(childFragmentManager, "DISK_TO_ADD_DIALOG")
            //viewModel.addDiskTitleToImport(DiskTitle(name = "What"))
        }
        setupButtonProceed()
        viewModel.disksToRent.observe(viewLifecycleOwner) {
            disksToRentAdapter.setDisksToRent(it)
            //Log.d(TAG, "disksToImport: $it")
        }
    }

    private fun setupButtonProceed() {
        binding.btnProceed.setOnClickListener {
            if (disksToRentAdapter.itemCount == 0 || binding.tvAddress.text.isEmpty() || binding.tvName.text.isEmpty() || binding.spinnerPhone.text.toString()
                    .isEmpty()
            ) Toast.makeText(
                requireActivity(),
                "You need to enter at least 1 disk title and fill all customer's information",
                Toast.LENGTH_LONG
            ).show()
            else {
                viewModel.setCustomerInformation(
                    selectedCustomerId,
                    binding.tvName.text.toString(),
                    binding.tvAddress.text.toString(),
                    binding.spinnerPhone.text.toString()
                )
                viewModel.proceedCustomer().observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is Response.Loading -> {
                        }
                        is Response.Success -> {

                        }
                        is Response.Failure -> {
                            Log.d(TAG, "Error: ${result.errorMessage}")
                        }
                    }
                }
                viewModel.addRental().observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is Response.Loading -> {
                        }
                        is Response.Success -> {
                            Toast.makeText(
                                requireActivity(),
                                "Rental added!",
                                Toast.LENGTH_LONG
                            ).show()
                            viewModel.clearDiskTitleToRent()

                            if (!isCurrentUserCustomer) {
                                findNavController().popBackStack()
                            }
                        }
                        is Response.Failure -> {
                            Log.d(TAG, "Error: ${result.errorMessage}")
                        }
                    }
                }
            }

        }

    }

    companion object {
        const val TAG = "NewRentalFrag"
        const val ARGUMENT_KEY_IS_USER_CUSTOMER = "ARGUMENT_KEY_IS_USER_CUSTOMER"

        viewModel.disksToRent.observe(viewLifecycleOwner) {
            disksToRentAdapter.setDisksToRent(it)
            //Log.d(TAG, "disksToImport: $it")
        }


        observeViewModel()

        setAutoCompleteTextViewPhone()
    }


    private fun setAutoCompleteTextViewPhone() {

        binding.spinnerPhone.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                binding.tvName.setText(customers[position].fullName)
                binding.tvAddress.setText(customers[position].address)
                selectedCustomerId = customers[position].id
            }
    }

    private fun observeViewModel() {
        viewModel.allCustomers.observe(viewLifecycleOwner) { allCustomers ->
            customers.clear()
            customers.addAll(allCustomers)

            binding.spinnerPhone.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    customers.map { it.phone })
            )
        }
    }
}