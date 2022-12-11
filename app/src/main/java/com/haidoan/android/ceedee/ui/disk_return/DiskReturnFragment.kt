package com.haidoan.android.ceedee.ui.disk_return

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalFiresoreDataSource
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalRepository
import com.haidoan.android.ceedee.databinding.FragmentDiskReturnBinding
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.DisksRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import com.haidoan.android.ceedee.ui.rental.adapters.DisksToReturnAdapter
import com.haidoan.android.ceedee.ui.report.util.toFormattedCurrencyString
import com.haidoan.android.ceedee.ui.report.util.toFormattedString
import java.time.LocalDate

private const val TAG = "DiskReturnFragment"

class DiskReturnFragment : Fragment() {
    private var _binding: FragmentDiskReturnBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: DiskReturnViewModel by viewModels {
        DiskReturnViewModel.Factory(
            DiskRentalRepository(DiskRentalFiresoreDataSource()),
            DiskTitlesRepository(requireActivity().application),
            DisksRepository(requireActivity().application),
            this
        )
    }

    private lateinit var diskTitlesToReturnAdapter: DisksToReturnAdapter
    private val navArgs: DiskReturnFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDiskReturnBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setRentalId(navArgs.currentRentalId)
        setupRecyclerview()
        setUpButtonProceed()
        observeViewModel()
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            //Log.d(TAG, "UiState: $uiState")
            binding.apply {
                textviewCustomerName.text = uiState.customerName
                textviewCustomerAddress.text = uiState.customerAddress
                textviewCustomerPhone.text = uiState.customerPhone
                textviewRentDate.text = uiState.rentDate?.toFormattedString()
                textviewDueDate.text = uiState.dueDate?.toFormattedString()

                if (uiState.overdueDateCount <= 0L) {
                    textviewOverdueFee.text = ""
                    textviewOverdueDateRange.text = ""
                } else {
                    textviewOverdueFee.text = uiState.overdueFee.toFormattedCurrencyString()
                    textviewOverdueDateRange.text = "${uiState.dueDate?.toFormattedString()} - ${
                        LocalDate.now().toFormattedString()
                    }"
                }
                textviewOverdueDateCount.text = "${uiState.overdueDateCount} days"
                textviewTotalPayment.text = uiState.totalPayment.toFormattedCurrencyString()
                diskTitlesToReturnAdapter.submitList(uiState.diskTitlesToReturn)
            }
        }
    }

    private fun setupRecyclerview() {
        diskTitlesToReturnAdapter = DisksToReturnAdapter()
        binding.recyclerviewDisksToReturn.adapter = diskTitlesToReturnAdapter
        binding.recyclerviewDisksToReturn.layoutManager = LinearLayoutManager(context)
    }

    private fun setUpButtonProceed() {
        binding.buttonProceed.setOnClickListener {
            viewModel.completeRental().observe(viewLifecycleOwner) { response ->
                when (response) {
                    is Response.Loading -> {
                        binding.linearlayoutContentWrapper.visibility = View.GONE
                        binding.progressbarImport.visibility = View.VISIBLE
                    }
                    is Response.Failure -> {}
                    is Response.Success -> {
                        findNavController().popBackStack()
                        Toast.makeText(
                            requireActivity(),
                            "New rental added!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}


