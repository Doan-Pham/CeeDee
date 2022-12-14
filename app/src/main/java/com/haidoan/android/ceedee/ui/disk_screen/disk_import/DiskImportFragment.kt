package com.haidoan.android.ceedee.ui.disk_screen.disk_import

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.disk_import.DiskImportFirestoreDataSource
import com.haidoan.android.ceedee.data.disk_import.DiskImportRepository
import com.haidoan.android.ceedee.data.disk_import.Import
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsFirestoreDataSource
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsRepository
import com.haidoan.android.ceedee.databinding.FragmentDiskImportBinding
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.DisksRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response


class DiskImportFragment : Fragment() {
    private var _binding: FragmentDiskImportBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: DiskImportViewModel by lazy {
        ViewModelProvider(
            this, DiskImportViewModel.Factory(
                DiskRequisitionsRepository(DiskRequisitionsFirestoreDataSource()),
                DiskTitlesRepository(requireActivity().application),
                DiskImportRepository(DiskImportFirestoreDataSource()),
                DisksRepository(requireActivity().application)
            )
        )[DiskImportViewModel::class.java]
    }

    private lateinit var disksToImportAdapter: DiskImportAdapter
    private val navArgs: DiskImportFragmentArgs by navArgs()

    private var currentRequisitionId = ""
    private val diskTitlesToImportAndAmount = mutableMapOf<String, Long>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDiskImportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        disksToImportAdapter = DiskImportAdapter()
        binding.recyclerviewDisksToImport.adapter = disksToImportAdapter
        binding.recyclerviewDisksToImport.layoutManager = LinearLayoutManager(context)

        viewModel.setCurrentRequisitionId(navArgs.requisitionId)
        viewModel.currentRequisition.observe(viewLifecycleOwner) { currentRequisition ->
            binding.textviewSupplierName.text = currentRequisition.supplierName
            binding.textviewSupplierEmail.text = currentRequisition.supplierEmail

            currentRequisitionId = currentRequisition.id

            diskTitlesToImportAndAmount.clear()
            diskTitlesToImportAndAmount.putAll(currentRequisition.diskTitlesToImport)

            if (currentRequisition.requisitionStatus == "Completed") {
                binding.buttonImport.visibility = View.GONE
                //binding.edittextTotalPayment.text = currentRequisition
            }
        }

        viewModel.disksToImport.observe(viewLifecycleOwner) {
            disksToImportAdapter.setDisksToImport(it)
        }
        setUpButtonImport()
    }

    private fun setUpButtonImport() {
        binding.apply {
            buttonImport.setOnClickListener {
                if (edittextTotalPayment.text.toString().trim() == "" ||
                    edittextTotalPayment.text.toString().toInt() < 0
                ) {
                    edittextTotalPayment.error = "Invalid input!"
                } else {
                    showImportConfirmationDialog { importDisks() }
                }
            }
        }
    }

    private fun importDisks() {
        viewModel.addNewImport(
            Import(
                requisitionId = currentRequisitionId,
                totalPayment = binding.edittextTotalPayment.text.toString().toLong()
            ),
            diskTitlesToImportAndAmount,
            currentRequisitionId
        ).observe(viewLifecycleOwner) { response ->
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
                        "Import Success!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showImportConfirmationDialog(onPositiveButtonClick: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Confirmation")
            .setMessage("Proceed with a new import? \nSupplier: ${binding.textviewSupplierName.text} \nTotal payment: ${binding.edittextTotalPayment.text}?")
            .setPositiveButton("Confirm") { _, _ -> onPositiveButtonClick() }
            .setNegativeButton("Cancel") { _, _ -> }
            .show()
    }

    // This dialog might be later upgraded to include the feature of taking photo of invoice
    private fun showDialogPayment() {
        val dialogBuilder = AlertDialog.Builder(context)
        val dialogLayout = layoutInflater.inflate(R.layout.dialog_import_payment, null)
        val editTextTotalPayment =
            dialogLayout.findViewById<EditText>(R.id.edittext_total_payment)

        editTextTotalPayment.requestFocus()

        val dialog = dialogBuilder
            .setTitle("Payment")
            .setView(dialogLayout)
            // Although this is overridden later, it's still included for older versions of
            // Android
            .setPositiveButton("Save") { _, _ ->
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .create()

        //This shows the keyboard immediately when the dialog opens
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()

        // This is so that the dialog doesn't dismiss automatically when clicking a button
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (editTextTotalPayment.text.toString().trim() == "" ||
                editTextTotalPayment.text.toString().toInt() < 0
            ) {
                editTextTotalPayment.error = "Invalid input!"
            } else {
                Toast.makeText(requireActivity(), "HA", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
        }
    }
}