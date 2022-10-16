package com.haidoan.android.ceedee

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.haidoan.android.ceedee.databinding.FragmentReportBinding
import com.haidoan.android.ceedee.ui.report.MonthYearXAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

private const val BAR_CHART_BAR_WIDTH = 0.45f
private const val BAR_CHART_BAR_SPACE = 0.02f
private const val BAR_CHAR_GROUP_SPACE = 0.06f
private const val BAR_CHART_MIN_X_DEFAULT = 0f

class ReportFragment : Fragment() {

    private lateinit var binding: FragmentReportBinding
    private lateinit var barChart: BarChart

    private var startMonth: Int = 10
    private var startYear: Int = 2022

    private var endMonth: Int = 10
    private var endYear: Int = 2022

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReportBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        barChart = binding.barChart

        fillBarChartData()
        drawBarChart()

        val monthFormatter = SimpleDateFormat("MM", Locale.US)
        val yearFormatter = SimpleDateFormat("yyyy", Locale.US)
        val date = Calendar.getInstance().time

        //startMonth = monthFormatter.format(date)
        //val startYear = yearFormatter.format(date)

        binding.textviewStartMonth.setOnClickListener {
            MonthYearPickerDialog(date).apply {
                setTitle("Select end month")
                setListener { _, month, year, _ ->
                    val displayedTime = "$month/$year"
                    binding.textviewStartMonth.text = displayedTime
                    startMonth = month
                    startYear = year
                    onMonthYearChanged()
                }
                show(this@ReportFragment.parentFragmentManager, "MonthYearPickerDialog")
            }
        }

        binding.textviewEndMonth.setOnClickListener {
            MonthYearPickerDialog(date).apply {
                setTitle("Select end month")
                setListener { _, month, year, _ ->
                    val displayedTime = "$month/$year"
                    binding.textviewEndMonth.text = displayedTime
                    endMonth = month
                    endYear = year
                    onMonthYearChanged()
                }
                show(this@ReportFragment.parentFragmentManager, "MonthYearPickerDialog")
            }
        }
    }

    private fun drawBarChart() {

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM;
        xAxis.setDrawGridLines(false)
        xAxis.setCenterAxisLabels(true)
        xAxis.granularity = 1f;
        xAxis.textSize = 12f
        xAxis.axisMinimum = BAR_CHART_MIN_X_DEFAULT

        // Needs to add 1 to show all bars
        xAxis.axisMaximum =
            getMonthCountBetween(startMonth, startYear, endMonth, endYear).toFloat() + 1
        xAxis.valueFormatter = MonthYearXAxisValueFormatter()

        val leftAxis: YAxis = barChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.textSize = 12f

        val rightAxis = barChart.axisRight
        rightAxis.isEnabled = false

        barChart.legend.form = Legend.LegendForm.CIRCLE
        barChart.legend.textSize = 12f

        barChart.setPinchZoom(false)
        barChart.description.isEnabled = false
        barChart.setVisibleXRangeMaximum(4f)
        barChart.groupBars(BAR_CHART_MIN_X_DEFAULT, BAR_CHAR_GROUP_SPACE, BAR_CHART_BAR_SPACE)
        barChart.invalidate()
    }

    private fun fillBarChartData() {

        //TODO: Add logic for retrieving data
        val entries = mutableListOf<BarEntry>()
        entries.add(BarEntry(0F, 1000F))
        entries.add(BarEntry(1F, 2000F))
        entries.add(BarEntry(2F, 500F))
        entries.add(BarEntry(3F, 500F))
        entries.add(BarEntry(4F, 500F))
        entries.add(BarEntry(5F, 500F))

        val entriesb = mutableListOf<BarEntry>()
        entriesb.add(BarEntry(0F, 3000F))
        entriesb.add(BarEntry(1F, 15000F))
        entriesb.add(BarEntry(2F, 800F))
        entriesb.add(BarEntry(3F, 800F))
        entriesb.add(BarEntry(4F, 800F))
        entriesb.add(BarEntry(5F, 800F))

        val dataSet = BarDataSet(entries, "Income")
        val dataSetB = BarDataSet(entriesb, "Expenses")
        dataSetB.color = Color.rgb(0, 0, 0)

        val barData = BarData(dataSet, dataSetB)
        barData.barWidth = BAR_CHART_BAR_WIDTH

        barChart.data = barData
    }

    private fun onMonthYearChanged() {
        if (startYear == endYear && startMonth == endMonth) return
        drawBarChart()
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