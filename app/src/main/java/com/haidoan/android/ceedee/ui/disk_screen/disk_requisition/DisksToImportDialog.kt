package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.DialogAddDiskToImportBinding
import com.haidoan.android.ceedee.ui.disk_screen.disk_import.DiskImportAdapter
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository

class DisksToImportDialog : DialogFragment() {

    companion object {
        const val TAG = "DisksToImportDialog"
    }

    private lateinit var binding: DialogAddDiskToImportBinding
    private val newRequisitionViewModel: NewRequisitionViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private val disksToImportViewModel: DisksToImportViewModel by lazy {
        ViewModelProvider(
            this, DisksToImportViewModel.Factory(
                DiskTitlesRepository(requireActivity().application),
            )
        )[DisksToImportViewModel::class.java]
    }

    private lateinit var disksToImportAdapter: DiskImportAdapter
    private val diskTitlesToImportAndAmount = mutableMapOf<String, Long>()


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogAddDiskToImportBinding.inflate(requireActivity().layoutInflater)


        disksToImportAdapter = DiskImportAdapter()
        binding.recyclerviewDisksToImport.adapter = disksToImportAdapter
        binding.recyclerviewDisksToImport.layoutManager = LinearLayoutManager(context)

        disksToImportViewModel.diskTitlesInStore.observe(this) { diskTitlesList ->
            val diskTitlesMap = mutableMapOf<DiskTitle, Long>()
            for (diskTitle in diskTitlesList) {
                diskTitlesMap[diskTitle] = diskTitle.diskAmount
            }
            Log.d(TAG, "diskTitlesMap $diskTitlesMap")
            disksToImportAdapter.setDisksToImport(diskTitlesMap)
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setPositiveButton("Ok") { _, _ ->
            }
            .setNegativeButton("Cancel") { _, _ -> dialog?.cancel() }
            .create()
    }

}