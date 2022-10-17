package com.haidoan.android.ceedee.ui.report

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.haidoan.android.ceedee.databinding.FragmentReportDiskBinding


private val CHART_COLOR_FIRST = Color.rgb(228, 86, 33)
private val CHART_COLOR_SECOND = Color.rgb(251, 173, 86)
private val CHART_COLOR_THIRD = Color.rgb(160, 215, 113)
private val CHART_COLOR_FOURTH = Color.rgb(115, 176, 215)

class ReportDiskFragment : Fragment() {

    private lateinit var binding: FragmentReportDiskBinding
    private lateinit var pieChart: PieChart

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
        fillPieChartData()
        styleAndDrawPieChart()
    }

    private fun styleAndDrawPieChart() {
        val l: Legend = pieChart.getLegend()
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
//        l.xEntrySpace = 7f
//        l.yEntrySpace = 0f
//        l.yOffset = 0f


        pieChart.setUsePercentValues(true)
        pieChart.isHighlightPerTapEnabled = false;
        pieChart.notifyDataSetChanged()
        pieChart.invalidate()
    }

    private fun fillPieChartData() {
        val entries = mutableListOf<PieEntry>()
        entries.add(PieEntry(100f, "In store"))
        entries.add(PieEntry(30f, "Rented"))
        entries.add(PieEntry(70f, "Broken"))

        val colors =
            listOf(CHART_COLOR_FIRST, CHART_COLOR_SECOND, CHART_COLOR_THIRD, CHART_COLOR_FOURTH)

        //pieChart.setUsePercentValues(true)
        val dataSet = PieDataSet(entries, "Disks by status")
        dataSet.colors = colors
        val data = PieData(dataSet)
        pieChart.data = data
        data.setValueFormatter(PercentFormatter())
        pieChart.data.setValueTextSize(14f)
    }
}