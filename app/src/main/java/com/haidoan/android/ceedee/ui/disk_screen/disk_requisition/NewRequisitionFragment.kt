package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

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
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsFirestoreDataSource
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsRepository
import com.haidoan.android.ceedee.data.supplier.Supplier
import com.haidoan.android.ceedee.data.supplier.SupplierFirestoreDataSource
import com.haidoan.android.ceedee.data.supplier.SupplierRepository
import com.haidoan.android.ceedee.databinding.FragmentNewRequisitionBinding
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response

private const val TAG = "NewRequisitionFrag"

class NewRequisitionFragment : Fragment() {

    private var _binding: FragmentNewRequisitionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: NewRequisitionViewModel by viewModels(
        factoryProducer = {
            NewRequisitionViewModel.Factory(
                DiskRequisitionsRepository(DiskRequisitionsFirestoreDataSource()),
                DiskTitlesRepository(requireActivity().application),
                SupplierRepository(SupplierFirestoreDataSource()),
            )
        })

    private lateinit var disksToImportAdapter: NewRequisitionDiskAdapter
    private val suppliers = mutableListOf<Supplier>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNewRequisitionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        disksToImportAdapter = NewRequisitionDiskAdapter(
            onButtonMinusClick = { diskTitle -> viewModel.decrementDiskTitleAmount(diskTitle) },
            onButtonPlusClick = { diskTitle -> viewModel.incrementDiskTitleAmount(diskTitle) },
            onButtonRemoveClick = { diskTitle -> viewModel.removeDiskTitleToImport(diskTitle) })

        binding.recyclerviewDisksToImport.adapter = disksToImportAdapter
        binding.recyclerviewDisksToImport.layoutManager = LinearLayoutManager(context)

        binding.spinnerSupplier.onItemSelectedListener = object : AdapterView.OnItemClickListener,
            AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.setSupplierOfNewRequisition(suppliers[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
            }

        }

        binding.buttonAddDisk.setOnClickListener {
            val disksToImportDialog = DisksToImportDialog()
            disksToImportDialog.show(childFragmentManager, "DISK_TO_IMPORT_DIALOG")
            //viewModel.addDiskTitleToImport(DiskTitle(name = "What"))
        }

        binding.buttonProceed.setOnClickListener {
            if (disksToImportAdapter.itemCount == 0) Toast.makeText(
                requireActivity(),
                "You need to enter at least 1 disk title",
                Toast.LENGTH_LONG
            ).show()
            else {
                viewModel.addRequisition().observe(viewLifecycleOwner) { result ->
                    when (result) {
                        is Response.Loading -> {
                            binding.linearlayoutContentWrapper.visibility = View.GONE
                            binding.progressbarImport.visibility = View.VISIBLE
                        }
                        is Response.Success -> {
                            findNavController().popBackStack()
                            Toast.makeText(
                                requireActivity(),
                                "Requisition sent!",
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
        viewModel.allSuppliers.observe(viewLifecycleOwner) { allSuppliers ->

            suppliers.clear()
            suppliers.addAll(allSuppliers)

            binding.spinnerSupplier.adapter = ArrayAdapter(
                requireActivity().baseContext,
                android.R.layout.simple_spinner_dropdown_item,
                suppliers.map { it.name }
            )
        }

        viewModel.disksToImport.observe(viewLifecycleOwner) {
            disksToImportAdapter.setDisksToImport(it)
            //Log.d(TAG, "disksToImport: $it")
        }
    }

}