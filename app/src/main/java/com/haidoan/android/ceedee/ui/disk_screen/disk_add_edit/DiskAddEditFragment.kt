package com.haidoan.android.ceedee.ui.disk_screen.disk_add_edit


import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import coil.load
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.Genre
import com.haidoan.android.ceedee.databinding.FragmentDiskAddEditBinding
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import com.haidoan.android.ceedee.ui.utils.PERMISSIONS
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.Serializable


class DiskAddEditFragment : Fragment() {

    private var filePath: Uri? = null
    private var coverImgUrl: String = ""
    private var genreId: String = ""
    private lateinit var diskTitle: DiskTitle

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

    @Suppress("DEPRECATION")
    inline fun <reified T : Serializable> Bundle.customGetSerializable(key: String): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSerializable(key, T::class.java)
        } else {
            getSerializable(key) as? T
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDiskAddEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        setListeners()

    }

    private fun getFromBundle() {
        if (arguments != null) {
            val diskTitle = arguments?.customGetSerializable<DiskTitle>("disk_title") as DiskTitle
            this.diskTitle = diskTitle

            bindImage(binding.imgDiskAddEditCoverImg, diskTitle.coverImageUrl)
            binding.edtDiskAddEditDiskTitleName.setText(diskTitle.name)
            binding.edtDiskAddEditAuthor.setText(diskTitle.author)
            binding.edtDiskAddEditDescription.setText(diskTitle.description)

            for (i in genreList.indices) {
                if (genreList[i].id == diskTitle.genreId) {
                    binding.spinnerDiskAddEditGenre.setSelection(i)
                    break
                }
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

                    getFromBundle()
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
            if (arguments != null) {
                updateDiskTitle()
            } else {
                addDiskTitle()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateDiskTitle() {
        if (binding.edtDiskAddEditDescription.text.toString() == ""
            || binding.edtDiskAddEditAuthor.text.toString() == ""
            || binding.edtDiskAddEditDiskTitleName.text.toString() == ""
        ) {
            binding.tvMessageRequiredAddEditDiskTitle.text = "Please fill all information!"
            binding.tvMessageRequiredAddEditDiskTitle.visibility = View.VISIBLE
        } else {
            binding.tvMessageRequiredAddEditDiskTitle.visibility = View.INVISIBLE
            updateDiskTitleToFireStore()
        }
    }

    private fun updateDiskTitleToFireStore() {
        binding.btnSave.visibility = View.GONE
        binding.progressBarDiskAddEditSave.visibility = View.VISIBLE
        if (filePath != null) {
            val fileName = filePath!!.path?.let { File(it).name }
            Log.d("TAG_test", "filepath $filePath, filename $fileName")
            diskAddEditViewModel.addImage(filePath, fileName)
                .observe(viewLifecycleOwner) { response ->
                    when (response) {
                        is Response.Loading -> {
                            binding.btnSave.visibility = View.GONE
                            binding.progressBarDiskAddEditSave.visibility = View.VISIBLE
                        }
                        is Response.Success -> {
                            val uri = response.data as Uri
                            Log.d("TAG_test", uri.toString())
                            binding.btnSave.visibility = View.VISIBLE
                            binding.progressBarDiskAddEditSave.visibility = View.GONE

                            coverImgUrl = uri.toString()
                            val author = binding.edtDiskAddEditAuthor.text.toString()
                            val description = binding.edtDiskAddEditDescription.text.toString()
                            val name = binding.edtDiskAddEditDiskTitleName.text.toString()
                            diskAddEditViewModel.updateDiskTitle(
                                diskTitle.id,
                                author,
                                coverImgUrl,
                                description,
                                genreId,
                                name
                            )
                                .observe(viewLifecycleOwner) { resp ->
                                    when (resp) {
                                        is Response.Loading -> {
                                            binding.btnSave.visibility = View.GONE
                                            binding.progressBarDiskAddEditSave.visibility =
                                                View.VISIBLE
                                        }
                                        is Response.Success -> {
                                            Toast.makeText(
                                                requireActivity(),
                                                "Update disk title success!!!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            view?.findNavController()?.popBackStack()
                                            binding.btnSave.visibility = View.VISIBLE
                                            binding.progressBarDiskAddEditSave.visibility =
                                                View.GONE
                                        }
                                        is Response.Failure -> {
                                            Toast.makeText(
                                                requireActivity(),
                                                "Fail to update disk title!!!",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            binding.btnSave.visibility = View.VISIBLE
                                            binding.progressBarDiskAddEditSave.visibility =
                                                View.GONE
                                        }
                                        else -> print(resp.toString())
                                    }
                                }
                        }
                        is Response.Failure -> {
                            binding.btnSave.visibility = View.VISIBLE
                            binding.progressBarDiskAddEditSave.visibility = View.GONE
                            Toast.makeText(
                                requireActivity(),
                                "Fail to update image to FireStore!!!",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                        else -> print(response.toString())
                    }
                }

        } else {
            coverImgUrl = diskTitle.coverImageUrl
            val author = binding.edtDiskAddEditAuthor.text.toString()
            val description = binding.edtDiskAddEditDescription.text.toString()
            val name = binding.edtDiskAddEditDiskTitleName.text.toString()
            diskAddEditViewModel.updateDiskTitle(
                diskTitle.id,
                author,
                coverImgUrl,
                description,
                genreId,
                name
            )
                .observe(viewLifecycleOwner) { resp ->
                    when (resp) {
                        is Response.Loading -> {
                            binding.btnSave.visibility = View.GONE
                            binding.progressBarDiskAddEditSave.visibility = View.VISIBLE
                        }
                        is Response.Success -> {
                            Toast.makeText(
                                requireActivity(),
                                "Update disk title success!!!",
                                Toast.LENGTH_SHORT
                            ).show()
                            view?.findNavController()?.popBackStack()
                            binding.btnSave.visibility = View.VISIBLE
                            binding.progressBarDiskAddEditSave.visibility = View.GONE
                        }
                        is Response.Failure -> {
                            Toast.makeText(
                                requireActivity(),
                                "Fail to update disk title!!!",
                                Toast.LENGTH_SHORT
                            ).show()

                            binding.btnSave.visibility = View.VISIBLE
                            binding.progressBarDiskAddEditSave.visibility = View.GONE
                        }
                        else -> print(resp.toString())
                    }
                }
        }
    }

    private fun addDiskTitle() {
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

    private fun addDiskTitleToFireStore() {
        val fileName = filePath!!.path?.let { File(it).name }
        binding.btnSave.visibility = View.GONE
        binding.progressBarDiskAddEditSave.visibility = View.VISIBLE
        if (filePath != null) {
            diskAddEditViewModel.addImage(filePath, fileName)
                .observe(viewLifecycleOwner) { response ->
                    when (response) {
                        is Response.Loading -> {
                            binding.btnSave.visibility = View.GONE
                            binding.progressBarDiskAddEditSave.visibility = View.VISIBLE
                        }
                        is Response.Success -> {
                            val uri = response.data as Uri
                            Log.d("TAG_test", uri.toString())
                            binding.btnSave.visibility = View.VISIBLE
                            binding.progressBarDiskAddEditSave.visibility = View.GONE

                            coverImgUrl = uri.toString()
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
                                .observe(viewLifecycleOwner) { resp ->
                                    when (resp) {
                                        is Response.Loading -> {
                                            binding.btnSave.visibility = View.GONE
                                            binding.progressBarDiskAddEditSave.visibility =
                                                View.VISIBLE
                                        }
                                        is Response.Success -> {
                                            Toast.makeText(
                                                requireActivity(),
                                                "Add disk title success!!!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            view?.findNavController()?.popBackStack()
                                            binding.btnSave.visibility = View.VISIBLE
                                            binding.progressBarDiskAddEditSave.visibility =
                                                View.GONE
                                        }
                                        is Response.Failure -> {
                                            Toast.makeText(
                                                requireActivity(),
                                                "Fail to add disk title!!!",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            binding.btnSave.visibility = View.VISIBLE
                                            binding.progressBarDiskAddEditSave.visibility =
                                                View.GONE
                                        }
                                        else -> print(resp.toString())
                                    }
                                }
                        }
                        is Response.Failure -> {
                            binding.btnSave.visibility = View.VISIBLE
                            binding.progressBarDiskAddEditSave.visibility = View.GONE
                            Toast.makeText(
                                requireActivity(),
                                "Fail to add image to FireStore!!!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> print(response.toString())
                    }
                }

        }
    }

    private fun loadImageFromUri(uri: Uri) {
        try {
            val imageStream: InputStream = requireActivity().contentResolver.openInputStream(uri)!!
            currentBitmap = BitmapFactory.decodeStream(imageStream)
            binding.imgDiskAddEditCoverImg.load(currentBitmap) {
                crossfade(true)
                placeholder(R.drawable.ic_disk_cover_placeholder_96)
                error(R.drawable.ic_disk_cover_placeholder_96)
            }
        } catch (e: FileNotFoundException) {
            Toast.makeText(
                requireActivity(),
                "Failed to load bitmap from gallery",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun bindImage(imgView: ImageView, imgUrl: String?) {
        imgUrl?.let {
            val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
            imgView.load(imgUri) {
                crossfade(true)
                placeholder(R.drawable.ic_disk_cover_placeholder_96)
                error(R.drawable.ic_disk_cover_placeholder_96)
            }
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