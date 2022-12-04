package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import android.content.Intent
import android.net.Uri
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsFirestoreDataSource
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsRepository
import com.haidoan.android.ceedee.data.supplier.Supplier
import com.haidoan.android.ceedee.data.supplier.SupplierFirestoreDataSource
import com.haidoan.android.ceedee.data.supplier.SupplierRepository
import com.haidoan.android.ceedee.databinding.FragmentNewRequisitionBinding
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository

private const val TAG = "NewRequisitionFrag"
private const val REQUISITION_EMAIL_SUBJECT = "Requisition for New Disk Titles "

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
    private var currentSupplier = Supplier()
    private var disksToImportAndAmount = mutableMapOf<DiskTitle, Long>()

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

        setUpRecyclerViewDisksToImport()
        setUpSpinnerSupplier()
        setUpButtonAddDisk()
        setUpButtonProceed()
        observeViewModel()
    }

    private fun setUpRecyclerViewDisksToImport() {
        disksToImportAdapter = NewRequisitionDiskAdapter(
            onButtonMinusClick = { diskTitle -> viewModel.decrementDiskTitleAmount(diskTitle) },
            onButtonPlusClick = { diskTitle -> viewModel.incrementDiskTitleAmount(diskTitle) },
            onButtonRemoveClick = { diskTitle -> viewModel.removeDiskTitleToImport(diskTitle) })

        binding.recyclerviewDisksToImport.adapter = disksToImportAdapter
        binding.recyclerviewDisksToImport.layoutManager = LinearLayoutManager(context)

    }

    private fun setUpSpinnerSupplier() {
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

    }

    private fun setUpButtonAddDisk() {
        binding.buttonAddDisk.setOnClickListener {
            val disksToImportDialog = DisksToImportDialog()
            disksToImportDialog.show(childFragmentManager, "DISK_TO_IMPORT_DIALOG")
            //viewModel.addDiskTitleToImport(DiskTitle(name = "What"))
        }
    }

    private fun setUpButtonProceed() {
        binding.buttonProceed.setOnClickListener {
            if (disksToImportAdapter.itemCount == 0) Toast.makeText(
                requireActivity(),
                "You need to enter at least 1 disk title",
                Toast.LENGTH_LONG
            ).show()
            else {
                sendEmailToSupplier()
//                viewModel.addRequisition().observe(viewLifecycleOwner) { result ->
//                    when (result) {
//                        is Response.Loading -> {
//                            binding.linearlayoutContentWrapper.visibility = View.GONE
//                            binding.progressbarImport.visibility = View.VISIBLE
//                        }
//                        is Response.Success -> {
//                            findNavController().popBackStack()
//                            Toast.makeText(
//                                requireActivity(),
//                                "Requisition sent!",
//                                Toast.LENGTH_LONG
//                            ).show()
//                        }
//                        is Response.Failure -> {
//                            Log.d(TAG, "Error: ${result.errorMessage}")
//                        }
//                    }
//                }
            }

        }
    }

    private fun sendEmailToSupplier() {
        val emailBodyStringBuilder =
            StringBuilder("Dear ${currentSupplier.name}, \n\nWe are CeeDee, and we would like to import the following disk titles: \n")

        for (diskTitleAndAmount in disksToImportAndAmount) {
            emailBodyStringBuilder.append("${diskTitleAndAmount.key.name} - ${diskTitleAndAmount.key.author}: ${diskTitleAndAmount.value} CD\n")
        }
        Log.d(TAG, "disksToImportAndAmount $disksToImportAndAmount")
        emailBodyStringBuilder
            .append("\n\nThank you for your time. We hope to receive a response from you soon.\n")
            .append("\nYours sincerely,\n\n")
            .append("CeeDee")

        composeEmail(
            arrayOf(currentSupplier.email),
            REQUISITION_EMAIL_SUBJECT,
            emailBodyStringBuilder.toString()
        )
    }

    private fun composeEmail(addresses: Array<String>, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, addresses)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
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
            disksToImportAndAmount = it
            //Log.d(TAG, "disksToImport: $it")
        }
        viewModel.supplierOfNewRequisition.observe(viewLifecycleOwner) { currentSupplier = it }
    }
}