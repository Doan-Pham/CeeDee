package com.haidoan.android.ceedee


import android.R
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
import com.google.firebase.firestore.*
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalFiresoreDataSource
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalRepository
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsFirestoreDataSource
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsRepository
import com.haidoan.android.ceedee.data.supplier.Supplier
import com.haidoan.android.ceedee.data.supplier.SupplierFirestoreDataSource
import com.haidoan.android.ceedee.data.supplier.SupplierRepository
import com.haidoan.android.ceedee.databinding.FragmentNewRentalScreenBinding
import com.haidoan.android.ceedee.fragmentRentalTabs.Adapters.NewRentalAdapter
import com.haidoan.android.ceedee.fragmentRentalTabs.DiskToRentDialog
import com.haidoan.android.ceedee.fragmentRentalTabs.ViewModels.NewRentalViewModel
import com.haidoan.android.ceedee.ui.disk_screen.disk_requisition.DisksToImportDialog
import com.haidoan.android.ceedee.ui.disk_screen.disk_requisition.NewRequisitionDiskAdapter
import com.haidoan.android.ceedee.ui.disk_screen.disk_requisition.NewRequisitionViewModel
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.android.synthetic.main.dialog_choose_disk.*
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val TAG = "NewRentalFrag"

class NewRentalScreen : Fragment() {
    private var _binding: FragmentNewRentalScreenBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: NewRentalViewModel by viewModels(
        factoryProducer = {
            NewRentalViewModel.Factory(
                DiskRentalRepository(DiskRentalFiresoreDataSource()),
                DiskTitlesRepository(requireActivity().application),
            )
        })
    private lateinit var disksToRentAdapter: NewRentalAdapter
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
        binding.btnProceed.setOnClickListener {
            if (disksToRentAdapter.itemCount == 0 || binding.tvAddress.text.isEmpty() || binding.tvName.text.isEmpty() || binding.tvPhone.text.isEmpty()) Toast.makeText(
                requireActivity(),
                "You need to enter at least 1 disk title and fill all customer's information",
                Toast.LENGTH_LONG
            ).show()
            else {
                viewModel.setCustomerInformation(
                    binding.tvName.text.toString(),
                    binding.tvAddress.text.toString(),
                    binding.tvPhone.text.toString()
                )
                viewModel.addRental().observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is Response.Loading -> {
                        }
                        is Response.Success -> {
                            findNavController().popBackStack()
                            Toast.makeText(
                                requireActivity(),
                                "Rental added!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        is Response.Failure -> {
                            Log.d(TAG, "Error: ${result.errorMessage}")
                        }
                    }
                }
            }

        }

        viewModel.disksToRent.observe(viewLifecycleOwner) {
            disksToRentAdapter.setDisksToRent(it)
            //Log.d(TAG, "disksToImport: $it")
        }
    }
}