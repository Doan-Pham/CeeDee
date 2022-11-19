package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.haidoan.android.ceedee.databinding.FragmentNewRequisitionBinding

private const val TAG = "NewRequisitionFrag"

class NewRequisitionFragment : Fragment() {

    private var _binding: FragmentNewRequisitionBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //    private val viewModel: NewRequisitionViewModel by lazy {
//        ViewModelProvider(
//            this, NewRequisitionViewModel.Factory(
//                NewRequisitionRepository(NewRequisitionFirestoreDataSource())
//            )
//        )[NewRequisitionViewModel::class.java]
//    }
    private lateinit var requisitionAdapter: DiskRequisitionAdapter

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

    }

}