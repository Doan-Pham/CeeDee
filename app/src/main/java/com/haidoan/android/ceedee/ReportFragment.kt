package com.haidoan.android.ceedee

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
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
        entriesb.add(BarEntry(1F, 6000F))
        entriesb.add(BarEntry(2F, 800F))

        val dataSet: BarDataSet = BarDataSet(entries, "Income")
        val dataSetB: BarDataSet = BarDataSet(entriesb, "Expenses")
        dataSet.color = R.color.primary
        dataSetB.color = R.color.secondary
        dataSet.valueTextColor = R.color.black
        barChart.data = BarData(dataSet, dataSetB)
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