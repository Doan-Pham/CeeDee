package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsFirestoreDataSource
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsRepository
import com.haidoan.android.ceedee.data.supplier.SupplierFirestoreDataSource
import com.haidoan.android.ceedee.data.supplier.SupplierRepository
import com.haidoan.android.ceedee.databinding.FragmentNewRequisitionBinding
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository

private const val TAG = "NewRequisitionFrag"

class NewRequisitionFragment : Fragment() {

    private var _binding: FragmentNewRequisitionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: NewRequisitionViewModel by lazy {
        ViewModelProvider(
            this, NewRequisitionViewModel.Factory(
                DiskRequisitionsRepository(DiskRequisitionsFirestoreDataSource()),
                DiskTitlesRepository(requireActivity().application),
                SupplierRepository(SupplierFirestoreDataSource()),
            )
        )[NewRequisitionViewModel::class.java]
    }

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

        viewModel.allSuppliers.observe(viewLifecycleOwner) { suppliers ->
            binding.spinnerSupplier.adapter = ArrayAdapter(
                requireActivity().baseContext,
                android.R.layout.simple_spinner_dropdown_item,
                suppliers.map { it.name }
            )
        }
    }

}