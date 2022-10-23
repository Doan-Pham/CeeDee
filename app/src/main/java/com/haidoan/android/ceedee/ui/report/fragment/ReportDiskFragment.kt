package com.haidoan.android.ceedee.ui.report.fragment

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
private val CHART_COLOR_FIFTH = Color.rgb(50, 50, 50)
private val CHART_COLOR_SIXTH = Color.rgb(100, 100, 100)
private val CHART_COLOR_SEVENTH = Color.rgb(150, 150, 150)
private val CHART_COLOR_EIGHTH = Color.rgb(200, 200, 200)
private val CHART_COLOR_NINE = Color.rgb(25, 25, 25)
private val CHART_COLOR_TEN = Color.rgb(0, 0, 0)

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
        val legend = pieChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        legend.isWordWrapEnabled = true
        legend.orientation = Legend.LegendOrientation.HORIZONTAL
        legend.xEntrySpace = 7f;
        legend.textSize = 12f
        legend.setDrawInside(false)

        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(14f);
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = false
        pieChart.isHighlightPerTapEnabled = false
        pieChart.invalidate()
    }

    private fun fillPieChartData() {
        val entries = mutableListOf<PieEntry>()
        entries.add(PieEntry(100f, "In store"))
        entries.add(PieEntry(30f, "Rented"))
        entries.add(PieEntry(70f, "Broken"))
        entries.add(PieEntry(90f, "Rock"))
        entries.add(PieEntry(20f, "Punk"))
        entries.add(PieEntry(50f, "Guitar"))
        entries.add(PieEntry(40f, "Country"))
        entries.add(PieEntry(30f, "Pop"))
        entries.add(PieEntry(20f, "Blues"))

        val colors =
            listOf(
                CHART_COLOR_FIRST, CHART_COLOR_SECOND, CHART_COLOR_THIRD, CHART_COLOR_FOURTH,
                CHART_COLOR_FIFTH, CHART_COLOR_SIXTH, CHART_COLOR_SEVENTH, CHART_COLOR_EIGHTH,
                CHART_COLOR_NINE, CHART_COLOR_TEN
            )

        val dataSet = PieDataSet(entries, "Disks by status")
        dataSet.colors = colors

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))
        data.setValueTextSize(14f)
        data.setValueTextColor(Color.WHITE)
        pieChart.data = data
    }
}