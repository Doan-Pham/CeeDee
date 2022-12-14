package com.haidoan.android.ceedee.ui.report.fragment

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FILTER_BITMAP_FLAG
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.report.FirestoreStatisticsDataSource
import com.haidoan.android.ceedee.data.report.ReportRepository
import com.haidoan.android.ceedee.databinding.FragmentRevenueExpensesBinding
import com.haidoan.android.ceedee.ui.report.util.ImageUtils.Companion.resizeBitmap
import com.haidoan.android.ceedee.ui.report.util.MonthYearPickerDialog
import com.haidoan.android.ceedee.ui.report.util.MonthYearXAxisValueFormatter
import com.haidoan.android.ceedee.ui.report.viewmodel.ReportViewModel
import com.haidoan.android.ceedee.ui.utils.*
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.*


private const val BAR_CHART_BAR_WIDTH = 0.45f
private const val BAR_CHART_BAR_SPACE = 0.02f
private const val BAR_CHAR_GROUP_SPACE = 0.06f
private const val BAR_CHART_MIN_X_DEFAULT = 0f
private const val CHART_TEXT_SIZE = 14f
private const val TAG = "RevenueExpensesFragment"

class RevenueExpensesFragment : Fragment() {

    private lateinit var binding: FragmentRevenueExpensesBinding

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
    private lateinit var barChart: BarChart
    private lateinit var lineChart: LineChart

    private var revenueDataCache: MutableMap<LocalDate, Float> = mutableMapOf()
    private var expensesDataCache: MutableMap<LocalDate, Float> = mutableMapOf()

    private var startTime: LocalDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
    private var endTime: LocalDate = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())

    private var currentChartTypeShown: ChartType = ChartType.BAR_CHART

    private lateinit var pdfFile: File

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRevenueExpensesBinding.inflate(inflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        builder.detectFileUriExposure()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barChart = binding.barChart
        lineChart = binding.lineChart

        styleLineChart()
        styleBarChart()

        lineChart.visibility = View.GONE

        setUpTextViewChooseStartTime()
        setUpTextViewChooseEndTime()
        setUpOptionMenu()
        setUpButtonPrint()

        viewModel.monthlyRevenue.observe(viewLifecycleOwner) {
            styleLineChart()
            fillLineChartData(it, null)
            styleBarChart()
            fillBarChartData(it, null)
            revenueDataCache.clear()
            revenueDataCache.putAll(it)
        }
        viewModel.monthlyExpenses.observe(viewLifecycleOwner) {
            styleLineChart()
            fillLineChartData(null, it)
            styleBarChart()
            fillBarChartData(null, it)
            expensesDataCache.clear()
            expensesDataCache.putAll(it)
        }
    }


    private fun setUpTextViewChooseStartTime() {
        val displayedStartTime = "${startTime.monthValue}/${startTime.year}"
        binding.textviewChooseStartMonth.text = displayedStartTime
        binding.textviewChooseStartMonth.setOnClickListener {
            MonthYearPickerDialog(Calendar.getInstance().time).apply {
                setTitle("Select start month")
                setListener { _, month, year, _ ->
                    if (year > endTime.year || (year == endTime.year && month > endTime.monthValue)) {
                        Toast.makeText(
                            requireActivity(),
                            "Error: Start time should be sooner or equal to end time",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val displayedTime = "$month/$year"
                        binding.textviewChooseStartMonth.text = displayedTime
                        startTime = startTime.withMonth(month).withYear(year)
                        onMonthYearChanged()
                    }
                }
                show(this@RevenueExpensesFragment.parentFragmentManager, "MonthYearPickerDialog")
            }
        }
    }

    private fun setUpTextViewChooseEndTime() {
        val displayedEndTime = "${endTime.monthValue}/${endTime.year}"
        binding.textviewChooseEndMonth.text = displayedEndTime
        binding.textviewChooseEndMonth.setOnClickListener {
            MonthYearPickerDialog(Calendar.getInstance().time).apply {
                setTitle("Select end month")
                setListener { _, month, year, _ ->
                    if (year < startTime.year || (year == startTime.year && month < startTime.monthValue)) {
                        Toast.makeText(
                            requireActivity(),
                            "Error: Start time should be earlier or the same as end time",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val displayedTime = "$month/$year"
                        binding.textviewChooseEndMonth.text = displayedTime
                        endTime = endTime.withMonth(month).withYear(year)
                        onMonthYearChanged()
                    }
                }
                show(
                    this@RevenueExpensesFragment.parentFragmentManager,
                    "MonthYearPickerDialog"
                )
            }
        }
    }

    private fun setUpButtonPrint() {
        binding.buttonPrint.setOnClickListener {
            // on below line we are checking permission
            if (!handlePermission()) return@setOnClickListener

            AlertDialog.Builder(requireContext())
                .setTitle("Important Note")
                .setMessage(R.string.chart_print_note)
                .setPositiveButton("Print") { _, _ ->
                    printReportAsPdf()
                    if (handlePermission())
                        openPdf()
                }
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
        val imagePaint = Paint(FILTER_BITMAP_FLAG)
        val textPaint = Paint()

        val chartAsBitmap =
            if (currentChartTypeShown == ChartType.LINE_CHART) lineChart.chartBitmap
            else barChart.chartBitmap

        val chartAsScaledBitmap =
            resizeBitmap(
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

        val monthYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM-yyyy")
        pageCanvas.drawText(
            "Revenue and expenses from ${monthYearFormatter.format(startTime)} to ${
                monthYearFormatter.format(
                    endTime
                )
            }",
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
        pageCanvas.drawText(
            "Total Revenue: ${
                DecimalFormat("#,###").format(
                    revenueDataCache.values.sum().toInt()
                )
            } VND",
            pageCanvas.width / 2f,
            pageCanvas.height - 100f,
            textPaint
        )
        pageCanvas.drawText(
            "Total Expenses: ${
                DecimalFormat("#,###").format(
                    expensesDataCache.values.sum().toInt()
                )
            } VND",
            pageCanvas.width / 2f,
            pageCanvas.height - 70f,
            textPaint
        )
        reportAsPdf.finishPage(firstPage)

        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
        val fileOutput = File(
            Environment.getExternalStorageDirectory(),
            "Report_Revenue_Expenses_${formatter.format(LocalDateTime.now())}.pdf"
        )
        pdfFile = fileOutput
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


    private fun openPdf() {
        val file = pdfFile
        val target = Intent(Intent.ACTION_VIEW)
        target.setDataAndType(Uri.fromFile(file), "application/pdf")
        target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY

        val intent = Intent.createChooser(target, "Open report file with")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            requireContext().startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Instruct the user to install a PDF reader here, or something
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

    private fun setUpOptionMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu_report_revenue_fragment, menu)
                menu.findItem(R.id.menu_item_rpfragment_chart_type).subMenu?.setHeaderTitle("Choose chart type")
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.menu_item_rpfragment_barchart -> {
                        if (currentChartTypeShown != ChartType.BAR_CHART) {
                            currentChartTypeShown = ChartType.BAR_CHART
                            barChart.visibility = View.VISIBLE
                            lineChart.visibility = View.GONE
                            requireActivity().invalidateOptionsMenu()
                        }
                        true
                    }
                    R.id.menu_item_rpfragment_linechart -> {
                        if (currentChartTypeShown != ChartType.LINE_CHART) {
                            currentChartTypeShown = ChartType.LINE_CHART
                            lineChart.visibility = View.VISIBLE
                            barChart.visibility = View.GONE
                            requireActivity().invalidateOptionsMenu()
                        }
                        true
                    }
                    else -> false
                }
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                menu.findItem(R.id.menu_item_rpfragment_chart_type).subMenu?.setHeaderTitle("Choose chart type")

                if (currentChartTypeShown == ChartType.BAR_CHART) {
                    menu.findItem(R.id.menu_item_rpfragment_chart_type)
                        .setIcon(R.drawable.ic_barchart_black_24dp)
                } else if (currentChartTypeShown == ChartType.LINE_CHART) {
                    menu.findItem(R.id.menu_item_rpfragment_chart_type)
                        .setIcon(R.drawable.ic_linechart_black_24dp)
                }

            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun styleLineChart() {
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.textSize = 14f
        xAxis.axisMinimum = BAR_CHART_MIN_X_DEFAULT
        xAxis.axisMaximum =
            if (getMonthCountBetween(startTime, endTime).toFloat() > 1)
                getMonthCountBetween(startTime, endTime).toFloat()
            else BAR_CHART_MIN_X_DEFAULT + 1
        xAxis.valueFormatter = MonthYearXAxisValueFormatter(startTime)
        xAxis.labelRotationAngle = -60f

        val leftAxis = lineChart.axisLeft
        leftAxis.textSize = CHART_TEXT_SIZE
        leftAxis.valueFormatter = LargeValueFormatter()

        lineChart.axisRight.isEnabled = false

        val legend = lineChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.form = Legend.LegendForm.LINE
        legend.formSize = CHART_TEXT_SIZE
        legend.textSize = CHART_TEXT_SIZE

        lineChart.setScaleEnabled(false)
        lineChart.isScaleXEnabled = true
        lineChart.isHighlightPerTapEnabled = false
        lineChart.isHighlightPerDragEnabled = false
        lineChart.description.isEnabled = false
        lineChart.extraRightOffset = 50f
        lineChart.extraBottomOffset = 10f

        //Have to call notifyDataSetChanged for the UI change to take place immediately
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()

        Log.d(TAG, "style line called")
    }

    private fun fillLineChartData(
        revenueDataNew: Map<LocalDate, Float>?,
        expensesDataNew: Map<LocalDate, Float>?
    ) {
        //Log.d(TAG, "Starting fillLineChartData()")

        // Use new data if it exists, else use data cache
        val revenueDataFinal: Map<LocalDate, Float> = revenueDataNew ?: revenueDataCache

        val revenueChartEntries = mutableListOf<Entry>()

        revenueDataFinal.forEach { (key, value) ->
            revenueChartEntries.add(
                Entry(
                    getMonthCountBetween(startTime, key).toFloat(),
                    value
                )
            )
        }
        //Log.d(TAG, "revenueDataFinal: $revenueDataFinal")

        val expensesChartEntries = mutableListOf<Entry>()

        // Use new data if it exists, else use cached data
        val expensesDataFinal: Map<LocalDate, Float> = expensesDataNew ?: expensesDataCache

        expensesDataFinal.forEach { (key, value) ->
            expensesChartEntries.add(
                Entry(
                    getMonthCountBetween(
                        startTime, key
                    ).toFloat(),
                    value
                )
            )
        }
        //Log.d(TAG, "expensesDataFinal: $expensesDataFinal")

        val dataSetRevenue = LineDataSet(revenueChartEntries, "Revenue")
        dataSetRevenue.color = CHART_COLOR_FIRST
        dataSetRevenue.axisDependency = YAxis.AxisDependency.LEFT

        val dataSetExpenses = LineDataSet(expensesChartEntries, "Expenses")
        dataSetExpenses.color = CHART_COLOR_SECOND
        dataSetExpenses.axisDependency = YAxis.AxisDependency.LEFT

        lineChart.clear()
        lineChart.data = LineData(dataSetRevenue, dataSetExpenses)
        lineChart.data.setValueTextSize(CHART_TEXT_SIZE)
        lineChart.data.setValueFormatter(LargeValueFormatter())
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()

        //Log.d(TAG, "fillLineChartData() called")
    }

    private fun styleBarChart() {
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setCenterAxisLabels(true)
        xAxis.granularity = 1f
        xAxis.textSize = 14f
        xAxis.axisMinimum = BAR_CHART_MIN_X_DEFAULT
        xAxis.axisMaximum =
            getMonthCountBetween(startTime, endTime).toFloat() + 1
        xAxis.valueFormatter = MonthYearXAxisValueFormatter(startTime)
        xAxis.labelRotationAngle = -60f

        val leftAxis = barChart.axisLeft
        leftAxis.textSize = CHART_TEXT_SIZE
        //leftAxis.setDrawZeroLine(true)
        leftAxis.spaceTop = 20f
        leftAxis.valueFormatter = LargeValueFormatter()

        barChart.axisRight.isEnabled = false

        val legend = barChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        legend.form = Legend.LegendForm.CIRCLE
        legend.formSize = CHART_TEXT_SIZE
        legend.textSize = CHART_TEXT_SIZE

        barChart.setScaleEnabled(false)
        barChart.isScaleXEnabled = true
        barChart.isHighlightPerTapEnabled = false
        barChart.isHighlightFullBarEnabled = false
        barChart.isHighlightPerDragEnabled = false
        barChart.description.isEnabled = false
        //barChart.setVisibleXRangeMaximum(1000f)
        barChart.extraRightOffset = 20f
        barChart.extraBottomOffset = 10f
    }

    private fun fillBarChartData(
        revenueDataNew: Map<LocalDate, Float>?,
        expensesDataNew: Map<LocalDate, Float>?
    ) {

        Log.d(TAG, "Starting fillBarChartData()")


        // Use new data if it exists, else use data cache
        val revenueDataFinal: Map<LocalDate, Float> = revenueDataNew ?: revenueDataCache

        val revenueChartEntries = mutableListOf<BarEntry>()

        revenueDataFinal.forEach { (key, value) ->
            revenueChartEntries.add(
                BarEntry(
                    getMonthCountBetween(
                        startTime, key
                    ).toFloat(),
                    value
                )
            )
        }
        Log.d(TAG, "revenueDataFinal: $revenueDataFinal")

        val expensesChartEntries = mutableListOf<BarEntry>()

        // Use new data if it exists, else use cached data
        val expensesDataFinal: Map<LocalDate, Float> = expensesDataNew ?: expensesDataCache

        expensesDataFinal.forEach { (key, value) ->
            expensesChartEntries.add(
                BarEntry(
                    getMonthCountBetween(startTime, key).toFloat(),
                    value
                )
            )
        }
        Log.d(TAG, "expensesDataFinal: $expensesDataFinal")

        val dataSetRevenue = BarDataSet(revenueChartEntries, "Revenue")
        dataSetRevenue.color = CHART_COLOR_FIRST

        val dataSetExpenses = BarDataSet(expensesChartEntries, "Expenses")
        dataSetExpenses.color = CHART_COLOR_SECOND

        val barData = BarData(dataSetRevenue, dataSetExpenses)
        barData.barWidth = BAR_CHART_BAR_WIDTH

        barChart.clear()
        barChart.data = barData
        barChart.data.setValueTextSize(CHART_TEXT_SIZE)
        barChart.data.setValueFormatter(LargeValueFormatter())

        // Need to call this after setting chart data
        barChart.groupBars(BAR_CHART_MIN_X_DEFAULT, BAR_CHAR_GROUP_SPACE, BAR_CHART_BAR_SPACE)
        barChart.notifyDataSetChanged()
        barChart.invalidate()
        Log.d(TAG, "fillBarChartData() called")
    }

    private fun onMonthYearChanged() {
        viewModel.setMonthsPeriod(startTime, endTime)
        Log.d(
            TAG,
            "onMonthYearChanged() called, startTime.monthValue: ${startTime.monthValue}, startTime.year: ${startTime.year}, endTime.monthValue: ${endTime.monthValue}, endTime.year: ${endTime.year}"
        )
    }

    private fun getMonthCountBetween(startTime: LocalDate, endTime: LocalDate): Long {
        return ChronoUnit.MONTHS.between(
            YearMonth.from(startTime),
            YearMonth.from(endTime)
        )
    }
}

private enum class ChartType {
    BAR_CHART, LINE_CHART
}


