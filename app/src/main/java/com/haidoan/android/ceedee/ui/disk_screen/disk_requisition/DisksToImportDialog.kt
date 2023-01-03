package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import android.app.Dialog
import android.os.Bundle
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidoan.android.ceedee.databinding.DialogAddDiskToImportBinding
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository

class DisksToImportDialog : DialogFragment() {

    companion object {
        const val TAG = "DisksToImportDialog"
    }

    private lateinit var binding: DialogAddDiskToImportBinding

    private val newRequisitionViewModel: NewRequisitionViewModel by viewModels(
        ownerProducer = { requireParentFragment() })

    private
    val disksToImportViewModel: DisksToImportViewModel by lazy {
        ViewModelProvider(
            this, DisksToImportViewModel.Factory(
                DiskTitlesRepository(requireActivity().application),
            )
        )[DisksToImportViewModel::class.java]
    }

    private lateinit var disksToImportAdapter: DisksToImportAdapter


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddDiskToImportBinding.inflate(requireActivity().layoutInflater)

        disksToImportAdapter = DisksToImportAdapter { diskTitle ->
            newRequisitionViewModel.addDiskTitleToImport(diskTitle)
        }
        binding.recyclerviewDisksToImport.adapter = disksToImportAdapter
        binding.recyclerviewDisksToImport.layoutManager = LinearLayoutManager(context)

        binding.searchviewDiskToImport.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                disksToImportViewModel.searchDiskTitle(newText)
                return false
            }
        })

        disksToImportViewModel.diskTitlesInStore.observe(this) {
            disksToImportAdapter.submitList(it)
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setNegativeButton("Finish") { _, _ -> dialog?.cancel() }
            .create()
    }

}