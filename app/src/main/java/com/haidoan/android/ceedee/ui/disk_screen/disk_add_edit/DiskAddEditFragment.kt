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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.haidoan.android.ceedee.data.Genre
import com.haidoan.android.ceedee.databinding.FragmentDiskAddEditBinding
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import com.haidoan.android.ceedee.ui.report.util.PERMISSIONS
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.*


class DiskAddEditFragment : Fragment() {

    private var filePath: Uri? = null
    private var coverImgUrl: String = ""
    private var genreId: String = ""

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

    private val pickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            // Handle the returned Uri

            uri?.let {
                loadImageFromUri(it)
                filePath = uri
                Log.d("TAG_REF", "filePath: $filePath")
            }
        }

    private var _binding: FragmentDiskAddEditBinding? = null

    private lateinit var diskAddEditViewModel: DiskAddEditViewModel

    private var genreList = mutableListOf<Genre>()
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

    private fun addDiskTitleToFireStore() {
        binding.btnSave.visibility = View.GONE
        binding.progressBarDiskAddEditSave.visibility = View.VISIBLE
        if (filePath != null) {
            val storageReference: StorageReference = FirebaseStorage.getInstance().reference
            val ref = storageReference.child("disk_titles_img/" + UUID.randomUUID().toString())
            val uploadTask = ref.putFile(filePath!!)

            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    coverImgUrl = downloadUri.toString()

                    val author = binding.edtDiskAddEditAuthor.text.toString()
                    val description = binding.edtDiskAddEditDescription.text.toString()
                    val name = binding.edtDiskAddEditDiskTitleName.text.toString()
                    diskAddEditViewModel.addDiskTitle(
                        author,
                        coverImgUrl,
                        description,
                        genreId,
                        name
                    )
                        .observe(viewLifecycleOwner) { response ->
                            when (response) {
                                is Response.Loading -> {
                                }
                                is Response.Success -> {
                                    Toast.makeText(
                                        requireActivity(),
                                        "Add disk title success!!!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    binding.btnSave.visibility = View.VISIBLE
                                    binding.progressBarDiskAddEditSave.visibility = View.GONE
                                }
                                is Response.Failure -> {
                                    Toast.makeText(
                                        requireActivity(),
                                        "Fail to disk title!!!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    binding.btnSave.visibility = View.VISIBLE
                                    binding.progressBarDiskAddEditSave.visibility = View.GONE
                                }
                                else -> print(response.toString())
                            }
                        }
                } else {
                    // Handle failures
                    binding.btnSave.visibility = View.VISIBLE
                    binding.progressBarDiskAddEditSave.visibility = View.GONE
                }
            }.addOnFailureListener {
                Log.d("TAG_REF", it.message.toString())
                binding.btnSave.visibility = View.VISIBLE
                binding.progressBarDiskAddEditSave.visibility = View.GONE
            }
        }
    }

    private fun init() {
        diskAddEditViewModel =
            ViewModelProvider(requireActivity())[DiskAddEditViewModel::class.java]
        diskAddEditViewModel.getGenres().observe(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {
                    binding.layoutAddEditDisk.visibility = View.GONE
                    binding.progressBarDiskAddEdit.visibility = View.VISIBLE
                }
                is Response.Success -> {
                    binding.layoutAddEditDisk.visibility = View.VISIBLE
                    binding.progressBarDiskAddEdit.visibility = View.GONE

                    val list = response.data
                    genreList.addAll(list)

                    adapterForSpinnerGenre = ArrayAdapter(
                        requireActivity().baseContext,
                        android.R.layout.simple_spinner_dropdown_item,
                        genreList
                    )

                    binding.apply {
                        spinnerDiskAddEditGenre.apply {
                            adapter = adapterForSpinnerGenre
                        }
                    }
                }
                is Response.Failure -> {
                    binding.layoutAddEditDisk.visibility = View.VISIBLE
                    binding.progressBarDiskAddEdit.visibility = View.GONE
                    print(response.errorMessage)
                }
                else -> print(response.toString())
            }
        }
    }

    private fun setListeners() {
        binding.imgDiskAddEditCoverImg.setOnClickListener {
            if (!handlePermission()) return@setOnClickListener

            pickerLauncher.launch("image/*")
        }

        binding.spinnerDiskAddEditGenre.onItemSelectedListener =
            object : AdapterView.OnItemClickListener,
                AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val genre = genreList[position]
                    genreId = genre.id
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemClick(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                }

            }

        binding.btnSave.setOnClickListener {
            if (filePath == null || binding.edtDiskAddEditDescription.text.toString() == ""
                || binding.edtDiskAddEditAuthor.text.toString() == ""
                || binding.edtDiskAddEditDiskTitleName.text.toString() == ""
            ) {
                binding.tvMessageRequiredAddEditDiskTitle.text = "Please fill all information!"
                binding.tvMessageRequiredAddEditDiskTitle.visibility = View.VISIBLE
            } else {
                binding.tvMessageRequiredAddEditDiskTitle.visibility = View.INVISIBLE
                addDiskTitleToFireStore()
            }
        }
    }

    private fun loadImageFromUri(uri: Uri) {
        try {
            val imageStream: InputStream = requireActivity().contentResolver.openInputStream(uri)!!
            currentBitmap = BitmapFactory.decodeStream(imageStream)
            binding.imgDiskAddEditCoverImg.load(currentBitmap)
        } catch (e: FileNotFoundException) {
            Toast.makeText(
                requireActivity(),
                "Failed to load bitmap from gallery",
                Toast.LENGTH_SHORT
            ).show()
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