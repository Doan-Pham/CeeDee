package com.haidoan.android.ceedee.ui.report.fragment

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.report.FirestoreStatisticsDataSource
import com.haidoan.android.ceedee.data.report.ReportRepository
import com.haidoan.android.ceedee.databinding.FragmentReportDiskBinding
import com.haidoan.android.ceedee.ui.report.util.*
import com.haidoan.android.ceedee.ui.report.viewmodel.ReportViewModel
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private const val TAG = "ReportDiskFragment.kt"

class ReportDiskFragment : Fragment() {

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

    private lateinit var binding: FragmentReportDiskBinding
    private lateinit var pieChart: PieChart

    private val viewModel: ReportViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(
            this, ReportViewModel.Factory(
                activity.application, ReportRepository(
                    FirestoreStatisticsDataSource()
                )
            )
        )[ReportViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentReportDiskBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pieChart = binding.pieChart
        setUpButtonPrint()
        styleAndDrawPieChart()

        viewModel.diskRelatedData.observe(requireActivity()) { data ->
            styleAndDrawPieChart()
            fillPieChartData(data)
        }
    }

    private fun styleAndDrawPieChart() {
        val legend = pieChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        legend.isWordWrapEnabled = true
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.xEntrySpace = 7f
        legend.textSize = 12f
        legend.setDrawInside(false)

        //pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(14f)
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = false
        pieChart.isHighlightPerTapEnabled = false
        pieChart.setUsePercentValues(false);
        pieChart.invalidate()
    }

    private fun fillPieChartData(diskData: Map<String, Int>) {
        val pieChartEntries = mutableListOf<PieEntry>()

        diskData.forEach { (key, value) ->
            pieChartEntries.add(
                PieEntry(
                    value.toFloat(),
                    key
                )
            )
        }

        val colors =
            listOf(
                CHART_COLOR_FIRST, CHART_COLOR_SECOND, CHART_COLOR_THIRD, CHART_COLOR_FOURTH,
                CHART_COLOR_FIFTH, CHART_COLOR_SIXTH, CHART_COLOR_SEVENTH, CHART_COLOR_EIGHTH,
                CHART_COLOR_NINE, CHART_COLOR_TEN
            )

        val dataSet = PieDataSet(pieChartEntries, "")
        dataSet.colors = colors

        val data = PieData(dataSet)
        //data.setValueFormatter(PercentFormatter(pieChart))
        data.setValueTextSize(14f)
        data.setValueTextColor(Color.WHITE)

        pieChart.clear()
        pieChart.data = data
        pieChart.notifyDataSetChanged()
        pieChart.invalidate()
    }

    private fun setUpButtonPrint() {
        binding.buttonPrint.setOnClickListener {
            // on below line we are checking permission
            if (!handlePermission()) return@setOnClickListener

            AlertDialog.Builder(requireContext())
                .setTitle("Important Note")
                .setMessage(R.string.chart_print_note)
                .setPositiveButton("Print") { _, _ -> printReportAsPdf() }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                .create()
                .show()

        }
    }

    private fun printReportAsPdf() {

        val reportAsPdf = PdfDocument()
        val pageInfo: PdfDocument.PageInfo? = PdfDocument.PageInfo.Builder(
            STANDARD_REPORT_PAGE_WIDTH, STANDARD_REPORT_PAGE_HEIGHT, 1
        ).create()
        val firstPage: PdfDocument.Page = reportAsPdf.startPage(pageInfo)
        val pageCanvas: Canvas = firstPage.canvas

        // two variables for paint "paint" is used
        // for drawing shapes and we will use "title"
        // for adding text in our PDF file.
        val imagePaint = Paint(Paint.FILTER_BITMAP_FLAG)
        val textPaint = Paint()

        val chartAsBitmap = pieChart.chartBitmap

        val chartAsScaledBitmap =
            ImageUtils.resizeBitmap(
                chartAsBitmap,
                pageCanvas.width - 2 * 40,
                pageCanvas.height * 2 / 3
            )

        pageCanvas.drawBitmap(
            chartAsScaledBitmap,
            (pageCanvas.width - chartAsScaledBitmap.width) / 2f,
            (pageCanvas.height - chartAsScaledBitmap.height) / 2f,
            imagePaint
        )

        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textPaint.color = ContextCompat.getColor(requireActivity(), R.color.black)
        textPaint.textAlign = Paint.Align.CENTER

        textPaint.textSize = 20f
        textPaint.isFakeBoldText = true

        pageCanvas.drawText(
            "Disk titles % by genre",
            pageCanvas.width / 2f,
            60f,
            textPaint
        )

        textPaint.textSize = 15f
        textPaint.isFakeBoldText = false
        pageCanvas.drawText(
            "Report Date: ${DateTimeFormatter.ofPattern("dd-MM-yyyy").format(LocalDate.now())}",
            pageCanvas.width / 2f,
            80f,
            textPaint
        )

        textPaint.textSize = 20f
        textPaint.isFakeBoldText = true
        reportAsPdf.finishPage(firstPage)

        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val fileOutput = File(
            Environment.getExternalStorageDirectory(),
            "Report_Disk_${formatter.format(LocalDateTime.now())}.pdf"
        )
        val outputStream = FileOutputStream(fileOutput)
        try {
            reportAsPdf.writeTo(outputStream)
            Toast.makeText(
                requireActivity(),
                "PDF file generated..",
                Toast.LENGTH_SHORT
            ).show()

        } catch (e: Exception) {
            Log.e(TAG, "Error generating report: ${e.message}")
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