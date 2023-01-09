package com.haidoan.android.ceedee.ui.disk_return

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalFirestoreDataSource
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalRepository
import com.haidoan.android.ceedee.databinding.FragmentDiskReturnBinding
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.DisksRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import com.haidoan.android.ceedee.ui.rental.adapters.DisksToReturnAdapter
import com.haidoan.android.ceedee.ui.utils.*
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


private const val TAG = "DiskReturnFragment"

class DiskReturnFragment : Fragment() {
    private var _binding: FragmentDiskReturnBinding? = null

    private val multiplePermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted: Map<String, Boolean> ->
            if (isGranted.containsValue(false)) {
                Toast.makeText(
                    requireActivity(),
                    "Application needs permission to print report",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: DiskReturnViewModel by viewModels {
        DiskReturnViewModel.Factory(
            DiskRentalRepository(DiskRentalFirestoreDataSource()),
            DiskTitlesRepository(requireActivity().application),
            DisksRepository(requireActivity().application),
            this
        )
    }

    private lateinit var diskTitlesToReturnAdapter: DisksToReturnAdapter
    private val navArgs: DiskReturnFragmentArgs by navArgs()

    private lateinit var pdfFile: File

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDiskReturnBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        builder.detectFileUriExposure()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setRentalId(navArgs.currentRentalId)
        setupRecyclerview()
        setUpButtonProceed()
        observeViewModel()
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            //Log.d(TAG, "UiState: $uiState")
            binding.apply {
                textviewCustomerName.text = uiState.customerName
                textviewCustomerAddress.text = uiState.customerAddress
                textviewCustomerPhone.text = uiState.customerPhone
                textviewRentDate.text = uiState.rentDate?.toFormattedString()
                textviewDueDate.text = uiState.dueDate?.toFormattedString()

                if (uiState.overdueDateCount <= 0L) {
                    textviewOverdueFee.text = ""
                    textviewOverdueDateRange.text = ""
                } else {
                    textviewOverdueFee.text = uiState.overdueFee.toFormattedCurrencyString()
                    textviewOverdueDateRange.text = "${uiState.dueDate?.toFormattedString()} - ${
                        LocalDate.now().toFormattedString()
                    }"
                }
                textviewOverdueDateCount.text = "${uiState.overdueDateCount} days"
                textviewTotalPayment.text = uiState.totalPayment.toFormattedCurrencyString()
                diskTitlesToReturnAdapter.submitList(uiState.diskTitlesToReturn)
            }
        }
    }

    private fun setupRecyclerview() {
        diskTitlesToReturnAdapter = DisksToReturnAdapter()
        binding.recyclerviewDisksToReturn.adapter = diskTitlesToReturnAdapter
        binding.recyclerviewDisksToReturn.layoutManager = LinearLayoutManager(context)
    }

    private fun setUpButtonProceed() {
        binding.buttonProceed.setOnClickListener {
            showConfirmationDialog {
                if (handlePermission()) {
                    printReportAsPdf()
                    openPdf()
                }
                viewModel.completeRental().observe(viewLifecycleOwner) { response ->
                    when (response) {
                        is Response.Loading -> {
                            binding.linearlayoutContentWrapper.visibility = View.GONE
                            binding.progressbar.visibility = View.VISIBLE
                        }
                        is Response.Failure -> {}
                        is Response.Success -> {
                            findNavController().popBackStack()
                            Toast.makeText(
                                requireActivity(),
                                "Disks returned!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }

        }
    }

    private fun openPdf() {
        val file = pdfFile
        val target = Intent(Intent.ACTION_VIEW)
        target.setDataAndType(Uri.fromFile(file), "application/pdf")
        target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY

        val intent = Intent.createChooser(target, "Open bill PDF file with")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            requireContext().startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Instruct the user to install a PDF reader here, or something
        }
    }

    @SuppressLint("CheckResult")
    private fun printReportAsPdf() {
        val customerName = binding.textviewCustomerName.text.toString()
        val customerPhone = binding.textviewCustomerPhone.text.toString()
        val customerAddress = binding.textviewCustomerAddress.text.toString()
        val customerRentDate = binding.textviewRentDate.text.toString()
        val customerDueDate = binding.textviewDueDate.text.toString()

        val overdueDateCount = binding.textviewOverdueDateCount.text.toString()
        val overdueFee = binding.textviewOverdueFee.text.toString()

        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH.mm")
        val totalPayment = binding.textviewTotalPayment.text.toString()

        val reportAsPdf = PdfDocument()
        val pageInfo: PdfDocument.PageInfo? = PdfDocument.PageInfo.Builder(
            BILL_PAGE_WIDTH, BILL_PAGE_HEIGHT, 1
        ).create()
        val firstPage: PdfDocument.Page = reportAsPdf.startPage(pageInfo)
        val pageCanvas: Canvas = firstPage.canvas

        // two variables for paint "paint" is used
        // for drawing shapes and we will use "title"
        // for adding text in our PDF file.
        val imagePaint = Paint(Paint.FILTER_BITMAP_FLAG)
        val textPaint = Paint()

        // Draw title Customer
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.color = ContextCompat.getColor(requireActivity(), R.color.black)
        textPaint.textAlign = Paint.Align.CENTER

        textPaint.textSize = 16f
        textPaint.isFakeBoldText = true

        var currentY = 40f
        pageCanvas.drawText(
            "CeeDee - CD Rental Shop",
            pageCanvas.width / 2f,
            currentY,
            textPaint
        )

        currentY += 20f
        pageCanvas.drawText(
            "Rental Bill",
            pageCanvas.width / 2f,
            currentY,
            textPaint
        )

        currentY += 20f
        // Draw Name
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.color = ContextCompat.getColor(requireActivity(), R.color.black)
        textPaint.textAlign = Paint.Align.CENTER

        textPaint.textSize = 14f
        textPaint.isFakeBoldText = false

        pageCanvas.drawText(
            "Date: ${formatter.format(LocalDateTime.now())}",
            pageCanvas.width / 2f,
            currentY,
            textPaint
        )


        currentY += 40f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.color = ContextCompat.getColor(requireActivity(), R.color.black)
        textPaint.textAlign = Paint.Align.LEFT

        textPaint.textSize = 12f
        textPaint.isFakeBoldText = true

        pageCanvas.drawText(
            "Customer name:",
            pageCanvas.width / 8f,
            currentY,
            textPaint
        )

        textPaint.color = ContextCompat.getColor(requireActivity(), R.color.dark_grey)
        textPaint.textSize = 12f
        textPaint.isFakeBoldText = false
        pageCanvas.drawText(
            customerName,
            pageCanvas.width * 4 / 8f,
            currentY,
            textPaint
        )

        currentY += 20f
        // Draw phone
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.color = ContextCompat.getColor(requireActivity(), R.color.black)
        textPaint.textAlign = Paint.Align.LEFT

        textPaint.textSize = 12f
        textPaint.isFakeBoldText = true

        pageCanvas.drawText(
            "Customer phone:",
            pageCanvas.width / 8f,
            currentY,
            textPaint
        )
        textPaint.color = ContextCompat.getColor(requireActivity(), R.color.dark_grey)
        textPaint.textSize = 12f
        textPaint.isFakeBoldText = false

        pageCanvas.drawText(
            customerPhone,
            pageCanvas.width * 4 / 8f,
            currentY,
            textPaint
        )

        currentY += 20f
        // Draw address
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.color = ContextCompat.getColor(requireActivity(), R.color.black)
        textPaint.textAlign = Paint.Align.LEFT

        textPaint.textSize = 12f
        textPaint.isFakeBoldText = true

        pageCanvas.drawText(
            "Customer address:",
            pageCanvas.width / 8f,
            currentY,
            textPaint
        )
        textPaint.color = ContextCompat.getColor(requireActivity(), R.color.dark_grey)
        textPaint.textSize = 12f
        textPaint.isFakeBoldText = false

        pageCanvas.drawText(
            customerAddress,
            pageCanvas.width * 4 / 8f,
            currentY,
            textPaint
        )


//        // Draw rent date
//        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
//        textPaint.color = ContextCompat.getColor(requireActivity(), R.color.black)
//        textPaint.textAlign = Paint.Align.LEFT
//
//        textPaint.textSize = 14f
//        textPaint.isFakeBoldText = true
//
//        pageCanvas.drawText(
//            "Rent Date:",
//            pageCanvas.width / 8f,
//            140f,
//            textPaint
//        )
//        textPaint.color = ContextCompat.getColor(requireActivity(), R.color.dark_grey)
//        textPaint.textSize = 14f
//        textPaint.isFakeBoldText = false
//
//        pageCanvas.drawText(
//            customerRentDate,
//            pageCanvas.width * 3 / 8f,
//            140f,
//            textPaint
//        )
//
//        // Draw due date
//        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
//        textPaint.color = ContextCompat.getColor(requireActivity(), R.color.black)
//        textPaint.textAlign = Paint.Align.LEFT
//
//        textPaint.textSize = 14f
//        textPaint.isFakeBoldText = true
//
//        pageCanvas.drawText(
//            "Due Date:",
//            pageCanvas.width / 8f,
//            160f,
//            textPaint
//        )
//        textPaint.color = ContextCompat.getColor(requireActivity(), R.color.dark_grey)
//        textPaint.textSize = 14f
//        textPaint.isFakeBoldText = false
//
//        pageCanvas.drawText(
//            customerDueDate,
//            pageCanvas.width * 3 / 8f,
//            160f,
//            textPaint
//        )

//        // Draw disk to import title
//        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
//        textPaint.color = ContextCompat.getColor(requireActivity(), R.color.black)
//        textPaint.textAlign = Paint.Align.CENTER
//
//        textPaint.textSize = 18f
//        textPaint.isFakeBoldText = true
//
//        pageCanvas.drawText(
//            "Disk to Import",
//            pageCanvas.width / 2f,
//            180f,
//            textPaint
//        )

        // Draw disk
        currentY += 40f
        diskTitlesToReturnAdapter.currentList.forEach { diskTitleAmountPayment ->
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textPaint.color = ContextCompat.getColor(requireActivity(), R.color.black)
            textPaint.textAlign = Paint.Align.LEFT

            textPaint.textSize = 14f
            textPaint.isFakeBoldText = false
            pageCanvas.drawText(
                diskTitleAmountPayment.first.name,
                pageCanvas.width / 8f,
                currentY,
                textPaint
            )
            pageCanvas.drawText(
                "${diskTitleAmountPayment.second} CD",
                pageCanvas.width * 4 / 8f,
                currentY,
                textPaint
            )
            pageCanvas.drawText(
                diskTitleAmountPayment.third.toFormattedCurrencyString(),
                pageCanvas.width * 6 / 8f,
                currentY,
                textPaint
            )
            currentY += 20f
        }
        currentY += 20f
        pageCanvas.drawText(
            "Overdue:",
            pageCanvas.width * 4 / 8f,
            currentY,
            textPaint
        )

        // Draw OverdueFee
        textPaint.color = ContextCompat.getColor(requireActivity(), R.color.dark_grey)
        textPaint.textSize = 14f
        textPaint.isFakeBoldText = false

        pageCanvas.drawText(
            if (overdueFee.isNotBlank()) overdueFee else "0 VND",
            pageCanvas.width * 6 / 8f,
            currentY,
            textPaint
        )

        // Draw TotalPayment
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.color = ContextCompat.getColor(requireActivity(), R.color.black)
        textPaint.textAlign = Paint.Align.LEFT

        textPaint.textSize = 18f
        textPaint.isFakeBoldText = true

        currentY += 20f
        pageCanvas.drawText(
            "Total:",
            pageCanvas.width * 4 / 8f,
            currentY,
            textPaint
        )
        textPaint.color = ContextCompat.getColor(requireActivity(), R.color.dark_grey)
        textPaint.textSize = 14f
        textPaint.isFakeBoldText = false

        pageCanvas.drawText(
            totalPayment,
            pageCanvas.width * 6 / 8f,
            currentY,
            textPaint
        )

        reportAsPdf.finishPage(firstPage)
        val fileOutput = File(
            Environment.getExternalStorageDirectory(),
            "Bill_${
                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").format(LocalDateTime.now())
            }.pdf"
        )
        pdfFile = fileOutput
        val outputStream = FileOutputStream(fileOutput)
        try {
            reportAsPdf.writeTo(outputStream)
        } catch (e: Exception) {
            // on below line we are displaying a toast message as fail to generate PDF
            Toast.makeText(
                requireActivity(),
                "Fail to generate PDF file..",
                Toast.LENGTH_SHORT
            )
                .show()
        }
        outputStream.flush()
        outputStream.close()
        reportAsPdf.close()
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

    private fun showConfirmationDialog(onPositiveButtonClick: () -> Unit) {
        AlertDialog.Builder(context)
            .setTitle("Confirmation")
            .setMessage("Return disks rented by customer: ${binding.textviewCustomerName.text} and print bill ?")
            .setPositiveButton("Confirm") { _, _ -> onPositiveButtonClick() }
            .setNegativeButton("Cancel") { _, _ -> }
            .show()
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


