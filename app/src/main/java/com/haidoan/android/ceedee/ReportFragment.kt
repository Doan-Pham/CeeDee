package com.haidoan.android.ceedee

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.haidoan.android.ceedee.databinding.FragmentReportBinding
import java.text.SimpleDateFormat
import java.util.*


class ReportFragment : Fragment() {

    private lateinit var binding: FragmentReportBinding
    private lateinit var barChart: BarChart

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

        val entries = mutableListOf<BarEntry>()
        entries.add(BarEntry(0F, 1000F))
        entries.add(BarEntry(1F, 2000F))
        entries.add(BarEntry(2F, 500F))

        val entriesb = mutableListOf<BarEntry>()
        entriesb.add(BarEntry(0F, 3000F))
        entriesb.add(BarEntry(1F, 15000F))
        entriesb.add(BarEntry(2F, 800F))

        val dataSet = BarDataSet(entries, "Income")
        val dataSetB = BarDataSet(entriesb, "Expenses")
        dataSetB.color = Color.rgb(255, 255, 255)

        val barData = BarData(dataSet, dataSetB)
        barData.barWidth = 0.45f
        barData.groupBars(0f, 0.06f, 0.02f)

        barChart.data = barData
        barChart.setPinchZoom(false)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM;
        xAxis.setDrawGridLines(false)
        xAxis.setCenterAxisLabels(true)
        xAxis.axisMinimum = 0f
        xAxis.granularity = 1f;
        xAxis.textSize = 14f

        //xAxis.axisLineColor = R.color.primary
//xAxis.valueFormatter =
        val leftAxis: YAxis = barChart.getAxisLeft()
        leftAxis.spaceTop = 35f
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true
        leftAxis.textSize = 14f

        val rightAxis = barChart.axisRight
        rightAxis.isEnabled = false
        barChart.invalidate()


        val monthFormatter = SimpleDateFormat("MM", Locale.US)
        val date = Calendar.getInstance().time

        binding.textviewStartMonth.setOnClickListener {
            MonthYearPickerDialog(date).apply {
                setListener { _, year, month, _ ->
                    val displayedTime = "$month/$year"
                    binding.textviewStartMonth.text = displayedTime
                }
                show(this@ReportFragment.parentFragmentManager, "MonthYearPickerDialog")
            }
        }

        binding.textviewEndMonth.setOnClickListener {
            MonthYearPickerDialog(date).apply {
                setListener { _, year, month, _ ->
                    val displayedTime = "$month/$year"
                    binding.textviewEndMonth.text = displayedTime
                }
                show(this@ReportFragment.parentFragmentManager, "MonthYearPickerDialog")
            }
        }
    }

}