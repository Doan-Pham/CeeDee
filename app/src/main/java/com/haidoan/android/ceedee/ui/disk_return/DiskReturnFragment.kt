package com.haidoan.android.ceedee.ui.disk_return

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalFiresoreDataSource
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalRepository
import com.haidoan.android.ceedee.databinding.FragmentDiskReturnBinding
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository

private const val TAG = "DiskReturnFragment"

class DiskReturnFragment : Fragment() {
    private var _binding: FragmentDiskReturnBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: DiskReturnViewModel by viewModels {
        DiskReturnViewModel.Factory(
            DiskRentalRepository(DiskRentalFiresoreDataSource()),
            DiskTitlesRepository(requireActivity().application),
            this
        )
    }

    private val navArgs: DiskReturnFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDiskReturnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setRentalId(navArgs.currentRentalId)
        viewModel.uiState.observe(viewLifecycleOwner) { diskReturnUiState ->
            Log.d(TAG, "UiState: $diskReturnUiState")
            binding.apply {
                textviewTest.text = diskReturnUiState.customerName
            }
        }
    }

}