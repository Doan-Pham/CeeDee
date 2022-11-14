package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.haidoan.android.ceedee.databinding.FragmentDiskRequisitionBinding


class DiskRequisitionFragment : Fragment() {

    private var _binding: FragmentDiskRequisitionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDiskRequisitionBinding.inflate(inflater, container, false)
        return binding.root
    }
}