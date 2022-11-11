package com.haidoan.android.ceedee.ui.disk_screen.disk_add_edit

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast

import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.haidoan.android.ceedee.data.Genre
import com.haidoan.android.ceedee.databinding.FragmentDiskAddEditBinding
import com.haidoan.android.ceedee.ui.disk_screen.disk_titles.DiskTitlesViewModel
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import com.haidoan.android.ceedee.ui.report.util.PERMISSIONS
import java.io.FileNotFoundException
import java.io.InputStream


class DiskAddEditFragment : Fragment() {

    private val multiplePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted: Map<String, Boolean> ->
            if (isGranted.containsValue(false)) {
                Toast.makeText(
                    requireActivity(),
                    "Application needs permission to load photo report",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private val pickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // Handle the returned Uri
        uri?.let { loadImageFromUri(it) }
    }

    private var _binding: FragmentDiskAddEditBinding? = null
    private lateinit var diskTitlesViewModel: DiskTitlesViewModel

    private lateinit var adapterForSpinnerGenre: ArrayAdapter<Genre>
    private lateinit var currentBitmap: Bitmap
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDiskAddEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        setListeners()
    }

    private fun init() {
        diskTitlesViewModel = ViewModelProvider(requireActivity())[DiskTitlesViewModel::class.java]

        diskTitlesViewModel.getGenres().observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {
                }
                is Response.Success -> {
                    val list = response.data

                    val genreList = mutableListOf<Genre>()
                    genreList.addAll(list)

                    adapterForSpinnerGenre = ArrayAdapter(
                        requireActivity().baseContext,
                        android.R.layout.simple_spinner_item,
                        genreList
                    )

                    binding.apply {
                        spinnerDiskAddEditGenre.apply {
                            adapter = adapterForSpinnerGenre
                        }
                    }
                }
                is Response.Failure -> {
                    print(response.errorMessage)
                }
                else -> print(response.toString())
            }
        }
    }

    private fun setListeners() {
        binding.imgDiskAddEditCoverImg.setOnClickListener{
            if (!handlePermission()) return@setOnClickListener

            pickerLauncher.launch("image/*")
        }

        binding.btnSave.setOnClickListener{

        }
    }

    private fun loadImageFromUri(uri: Uri) {
        try {
            val imageStream: InputStream = requireActivity().contentResolver.openInputStream(uri)!!
            currentBitmap = BitmapFactory.decodeStream(imageStream)
            binding.imgDiskAddEditCoverImg.load(currentBitmap)
        } catch (e: FileNotFoundException) {
            Toast.makeText(requireActivity(), "Failed to load bitmap from gallery", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(
                    requireActivity(),
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("PERMISSIONS", "Permission is not granted: $permission")
                return false
            }
            Log.d("PERMISSIONS", "Permission already granted: $permission")
        }
        return true
    }

    // on below line we are creating a function to request permission.
    private fun handlePermission(): Boolean {
        return when {
            hasPermissions(PERMISSIONS) -> {
                true
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                multiplePermissionLauncher.launch(
                    PERMISSIONS
                )
                false
            }
        }
    }
}