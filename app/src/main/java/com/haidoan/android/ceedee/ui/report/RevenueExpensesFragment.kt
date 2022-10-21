package com.haidoan.android.ceedee.ui.report

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
import com.haidoan.android.ceedee.MonthYearPickerDialog
import com.haidoan.android.ceedee.databinding.FragmentRevenueExpensesBinding
import java.time.LocalDate
import java.util.*

private const val BAR_CHART_BAR_WIDTH = 0.45f
private const val BAR_CHART_BAR_SPACE = 0.02f
private const val BAR_CHAR_GROUP_SPACE = 0.06f
private const val BAR_CHART_MIN_X_DEFAULT = 0f
private const val CHART_TEXT_SIZE = 12f

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

    private val chartEntries = HashMap<LocalDate, Float>()

    private var startMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var startYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    private var endMonth: Int = startMonth
    private var endYear: Int = startYear

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

        val displayedStartTime = "$startMonth/$startYear"
        binding.textviewStartMonth.text = displayedStartTime

        val displayedEndTime = "$endMonth/$endYear"
        binding.textviewEndMonth.text = displayedEndTime

        binding.textviewStartMonth.setOnClickListener {
            MonthYearPickerDialog(Calendar.getInstance().time).apply {
                setTitle("Select start month")
                setListener { _, month, year, _ ->
                    if (year > endYear || (year == endYear && month > endMonth)) {
                        Toast.makeText(
                            requireActivity(),
                            "Error: Start time should be sooner or equal to end time",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val displayedTime = "$month/$year"
                        binding.textviewStartMonth.text = displayedTime
                        startMonth = month
                        startYear = year
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
                    if (year < startYear || (year == startYear && month < startMonth)) {
                        Toast.makeText(
                            requireActivity(),
                            "Error: Start time should be earlier or the same as end time",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val displayedTime = "$month/$year"
                        binding.textviewEndMonth.text = displayedTime
                        endMonth = month
                        endYear = year
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
                fillLineChartData(it)
            } else {

            }
        }
    }

    private fun styleLineChart() {
        val xAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM;
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f;
        xAxis.textSize = 14f
        xAxis.axisMinimum = BAR_CHART_MIN_X_DEFAULT
        xAxis.axisMaximum =
            getMonthCountBetween(startMonth, startYear, endMonth, endYear).toFloat() + 1
        xAxis.valueFormatter = MonthYearXAxisValueFormatter()

        val leftAxis = lineChart.axisLeft
        leftAxis.textSize = CHART_TEXT_SIZE
        leftAxis.spaceTop = 20f
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
        //Have to call notifyDataSetChanged for the UI change to take place immediately
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()

        Log.d("RevenueExpensesFragment", "style line called")
    }

    private fun fillLineChartData(dataEntries: Map<LocalDate, Float>?) {
        val actualEntries = mutableListOf<Entry>()
        dataEntries?.forEach { (key, value) ->
            actualEntries.add(
                Entry(
                    getMonthCountBetween(
                        startMonth,
                        startYear,
                        key.monthValue,
                        key.year,
                    ).toFloat(),
                    value
                )
            )
        }
        Log.d("RevenueExpensesFragment", dataEntries?.entries.toString())
        Log.d("RevenueExpensesFragment", "something")
        val entriesb = mutableListOf<Entry>()
        entriesb.add(Entry(0F, 3000F))
        entriesb.add(Entry(1F, -1500000f))
        entriesb.add(Entry(2F, 800F))
        entriesb.add(Entry(3F, 800F))
        entriesb.add(Entry(4F, 800F))
        entriesb.add(Entry(5F, 800F))

        val dataSet = LineDataSet(actualEntries, "Revenue")
        dataSet.color = CHART_COLOR_FIRST
        dataSet.axisDependency = YAxis.AxisDependency.LEFT;

        val dataSetB = LineDataSet(entriesb, "Expenses")
        dataSetB.color = CHART_COLOR_SECOND
        dataSetB.axisDependency = YAxis.AxisDependency.LEFT;

        lineChart.clear()
        lineChart.data = LineData(dataSet, dataSetB)
        lineChart.data.setValueTextSize(CHART_TEXT_SIZE)
        lineChart.data.setValueFormatter(LargeValueFormatter())
        lineChart.notifyDataSetChanged()
        lineChart.invalidate()
        Log.d("RevenueExpensesFragment", "fill line called")
    }

    private fun styleAndDrawBarChart() {
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM;
        xAxis.setDrawGridLines(false)
        xAxis.setCenterAxisLabels(true)
        xAxis.granularity = 1f;
        xAxis.textSize = 14f
        xAxis.axisMinimum = BAR_CHART_MIN_X_DEFAULT
        // Needs to add 1 to show all bars
        xAxis.axisMaximum =
            getMonthCountBetween(startMonth, startYear, endMonth, endYear).toFloat()
        xAxis.valueFormatter = MonthYearXAxisValueFormatter()

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
        barChart.data.setValueTextSize(CHART_TEXT_SIZE)
        barChart.isHighlightPerTapEnabled = false
        barChart.description.isEnabled = false
        barChart.setVisibleXRangeMaximum(4f)
        barChart.groupBars(BAR_CHART_MIN_X_DEFAULT, BAR_CHAR_GROUP_SPACE, BAR_CHART_BAR_SPACE)
        barChart.invalidate()
    }

    private fun fillBarChartData() {

        //TODO: Add logic for retrieving data
        val entries = mutableListOf<BarEntry>()
        entries.add(BarEntry(0F, 2000000F))
        entries.add(BarEntry(1F, 2000000F))
        entries.add(BarEntry(2F, 1500000F))
        entries.add(BarEntry(3F, 500F))
        entries.add(BarEntry(4F, 500F))
        entries.add(BarEntry(5F, 500F))

        val entriesb = mutableListOf<BarEntry>()
        entriesb.add(BarEntry(0F, 3000F))
        entriesb.add(BarEntry(1F, -1500000f))
        entriesb.add(BarEntry(2F, 800F))
        entriesb.add(BarEntry(3F, 800F))
        entriesb.add(BarEntry(4F, 800F))
        entriesb.add(BarEntry(5F, 800F))

        val dataSet = BarDataSet(entries, "Income")
        dataSet.color = CHART_COLOR_FIRST
        val dataSetB = BarDataSet(entriesb, "Expenses")
        dataSetB.color = CHART_COLOR_SECOND

        val barData = BarData(dataSet, dataSetB)
        barData.barWidth = BAR_CHART_BAR_WIDTH

        barChart.data = barData
    }

    private fun onMonthYearChanged() {
        if (startYear == endYear && startMonth == endMonth) return
        viewModel.setMonthsPeriod(
            LocalDate.of(startYear, startMonth, 15),
            LocalDate.of(endYear, endMonth, 15)
        )
    }

    private fun getMonthCountBetween(
        startMonth: Int,
        startYear: Int,
        endMonth: Int,
        endYear: Int
    ): Int {
        return (endYear - startYear) * 12 + (endMonth - startMonth)
    }
}


