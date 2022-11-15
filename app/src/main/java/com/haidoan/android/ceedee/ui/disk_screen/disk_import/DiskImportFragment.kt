package com.haidoan.android.ceedee.ui.disk_screen.disk_import

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
    }
}