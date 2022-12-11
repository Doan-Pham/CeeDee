package com.haidoan.android.ceedee.ui.disk_screen.disks

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.haidoan.android.ceedee.data.Disk
import com.haidoan.android.ceedee.data.DiskStatus
import com.haidoan.android.ceedee.data.Genre
import com.haidoan.android.ceedee.databinding.FragmentDiskTabDisksBinding
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskStatusRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.GenreRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.android.synthetic.main.activity_main.*

class DisksTabFragment : Fragment() {

    private lateinit var diskAdapter: DiskAdapter
    private lateinit var diskViewModel: DiskViewModel

    private lateinit var diskStatusAdapter: DiskStatusAdapter

    private var _binding: FragmentDiskTabDisksBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDiskTabDisksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    fun init() {
        diskViewModel = ViewModelProvider(requireActivity())[DiskViewModel::class.java]

        diskAdapter = DiskAdapter(requireActivity(), diskViewModel, viewLifecycleOwner, this)
        diskStatusAdapter = DiskStatusAdapter(requireActivity(), diskViewModel,viewLifecycleOwner,diskAdapter,binding)

        diskViewModel.getDisks().observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {
                    //Load a ProgressBar
                    binding.progressbarDisk.visibility = View.VISIBLE
                    Log.d("TAG_LIST", "LOADING...")
                }
                is Response.Success -> {
                    val list = response.data
                    //Do what you need to do with your list
                    diskAdapter.submitList(list.toMutableList())
                    Log.d("TAG_LIST", list.toString())
                    //Hide the ProgressBar
                    binding.progressbarDisk.visibility = View.GONE
                }
                is Response.Failure -> {
                    print(response.errorMessage)
                    //Hide the ProgressBar
                    binding.progressbarDisk.visibility = View.GONE
                    Log.d("TAG_LIST", "FAILURE " + response.errorMessage)
                }
                else -> print(response.toString())
            }
        }

        diskViewModel.getDiskStatus().observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {
                    //Load a ProgressBar
                    binding.progressbarDisk.visibility = View.VISIBLE
                    Log.d("TAG", "LOADING...")
                }
                is Response.Success -> {
                    val list = response.data

                    val diskStatusList = mutableListOf<DiskStatus>()
                    diskStatusList.add(DiskStatus(DiskStatusRepository.defaultDiskStatus, "All"))
                    diskStatusList.addAll(list)
                    diskStatusAdapter.submitList(diskStatusList)
                }
                is Response.Failure -> {
                    print(response.errorMessage)
                }
                else -> print(response.toString())
            }
        }

        binding.apply {
            rcvDisk.apply {
                layoutManager = LinearLayoutManager(activity)
                adapter = diskAdapter
            }
            rcvStatus.apply {
                layoutManager = LinearLayoutManager(activity).apply {
                    orientation = LinearLayoutManager.HORIZONTAL
                }
                adapter = diskStatusAdapter
            }
            (rcvStatus.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}