package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.haidoan.android.ceedee.databinding.FragmentBottomSheetNewRequisitionBinding

class NewRequisitionBottomSheetFragment(
    private val onButtonSendEmailClick: () -> Unit,
    private val onButtonAddRequisitionClick: () -> Unit
) :
    BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomSheetNewRequisitionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBottomSheetNewRequisitionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textviewSendEmail.setOnClickListener { onButtonSendEmailClick() }
        binding.textviewAddNewRequisition.setOnClickListener { onButtonAddRequisitionClick() }
    }
}