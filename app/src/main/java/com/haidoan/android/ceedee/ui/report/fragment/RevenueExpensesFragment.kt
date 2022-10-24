package com.haidoan.android.ceedee.ui.report.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.haidoan.android.ceedee.data.report.FirestoreApi
import com.haidoan.android.ceedee.data.report.ReportRepository
import com.haidoan.android.ceedee.databinding.FragmentRevenueExpensesBinding
import com.haidoan.android.ceedee.ui.report.util.MonthYearPickerDialog
import com.haidoan.android.ceedee.ui.report.util.MonthYearXAxisValueFormatter
import com.haidoan.android.ceedee.ui.report.viewmodel.ReportViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.*

private const val BAR_CHART_BAR_WIDTH = 0.45f
private const val BAR_CHART_BAR_SPACE = 0.02f
private const val BAR_CHAR_GROUP_SPACE = 0.06f
private const val BAR_CHART_MIN_X_DEFAULT = 0f
private const val CHART_TEXT_SIZE = 12f
private const val TAG = "RevenueExpensesFragment"

// Some methods for chart styling doesn't allow R.color
private val CHART_COLOR_FIRST = Color.rgb(228, 86, 33)
private val CHART_COLOR_SECOND = Color.rgb(251, 173, 86)
private val CHART_COLOR_THIRD = Color.rgb(160, 215, 113)
private val CHART_COLOR_FOURTH = Color.rgb(115, 176, 215)

class RevenueExpensesFragment : Fragment() {

    private lateinit var binding: FragmentRevenueExpensesBinding

    private val viewModel: ReportViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProvider(
            this, ReportViewModel.Factory(
                activity.application, ReportRepository(
                    FirestoreApi()
                )
            )
        )[ReportViewModel::class.java]
    }

    private lateinit var barChart: BarChart
    private lateinit var lineChart: LineChart

    private var revenueDataCache: MutableMap<LocalDate, Float> = mutableMapOf()
    private var expensesDataCache: MutableMap<LocalDate, Float> = mutableMapOf()

    private var startTime: LocalDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
    private var endTime: LocalDate = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRevenueExpensesBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barChart = binding.barChart
        lineChart = binding.lineChart
        styleLineChart()
        //styleBarChart()

        val displayedStartTime = "${startTime.monthValue}/${startTime.year}"
        binding.textviewStartMonth.text = displayedStartTime

        val displayedEndTime = "${endTime.monthValue}/${endTime.year}"
        binding.textviewEndMonth.text = displayedEndTime

        binding.textviewStartMonth.setOnClickListener {
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
                        binding.textviewStartMonth.text = displayedTime
                        startTime = startTime.withMonth(month).withYear(year)
                        onMonthYearChanged()
                    }
                }
                show(this@RevenueExpensesFragment.parentFragmentManager, "MonthYearPickerDialog")
            }
        }

        binding.textviewEndMonth.setOnClickListener {
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
                        binding.textviewEndMonth.text = displayedTime
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

        viewModel.monthlyRevenue.observe(viewLifecycleOwner) {
            if (lineChart.visibility == View.VISIBLE) {
                styleLineChart()
                fillLineChartData(it, null)
            } else if (barChart.visibility == View.VISIBLE) {
                styleBarChart()
                fillBarChartData(it, null)
            }
            revenueDataCache.clear()
            revenueDataCache.putAll(it)
        }

        viewModel.monthlyExpenses.observe(viewLifecycleOwner) {
            if (lineChart.visibility == View.VISIBLE) {
                styleLineChart()
                fillLineChartData(null, it)
            } else if (barChart.visibility == View.VISIBLE) {
                styleBarChart()
                fillBarChartData(null, it)
            }
            expensesDataCache.clear()
            expensesDataCache.putAll(it)
        }
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

        Log.d(TAG, "Line chart xAxis max: ${xAxis.axisMaximum}")
        xAxis.valueFormatter = MonthYearXAxisValueFormatter(startTime)
        //xAxis.setAvoidFirstLastClipping(true)

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
        lineChart.isHighlightPerTapEnabled = false
        lineChart.isHighlightPerDragEnabled = false
        lineChart.description.isEnabled = false
        lineChart.setVisibleXRangeMaximum(4f)
        lineChart.extraRightOffset = 40f
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
        Log.d(TAG, "Starting fillLineChartData()")

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
        Log.d(TAG, "revenueDataFinal: $revenueDataFinal")

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
        Log.d(TAG, "expensesDataFinal: $expensesDataFinal")

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

        Log.d(TAG, "fillLineChartData() called")
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
        barChart.isHighlightPerTapEnabled = false
        barChart.isHighlightFullBarEnabled = false
        barChart.isHighlightPerDragEnabled = false
        barChart.description.isEnabled = false
        barChart.setVisibleXRangeMaximum(4f)
        barChart.extraRightOffset = 20f
        barChart.extraBottomOffset = 10f
    }

    private fun fillBarChartData(
        revenueDataNew: Map<LocalDate, Float>?,
        expensesDataNew: Map<LocalDate, Float>?
    ) {
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
        Log.d(TAG, revenueDataFinal.entries.toString())

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


