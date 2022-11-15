package com.haidoan.android.ceedee.ui.disk_screen.disk_import

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidoan.android.ceedee.databinding.FragmentDiskImportBinding

class DiskImportFragment : Fragment() {
    private var _binding: FragmentDiskImportBinding? = null

//    private val viewModel: DiskImportViewModel by lazy {
//        ViewModelProvider(
//            this, DiskRequisitionsViewModel.Factory(
//                DiskRequisitionsRepository(DiskRequisitionsFirestoreDataSource())
//            )
//        )[DiskRequisitionsViewModel::class.java]
//    }


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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
        val disksToImportAdapter = DiskImportAdapter(
            hashMapOf(
                "My my my" to 4,
                "Ye" to 5,
                "My my mya" to 4,
                "My my myb" to 4,
                "My my myc" to 4,
                "My my myd" to 4,
                "My my mye" to 4,
                "My my myf" to 4,
                "My my myg" to 4
            )
        )
        binding.recyclerviewDisksToImport.adapter = disksToImportAdapter
        binding.recyclerviewDisksToImport.layoutManager = LinearLayoutManager(context)
    }
}