package com.haidoan.android.ceedee.ui.report.util

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter

class MonthYearXAxisValueFormatter(
    private val startMonth: Int,
    private val startYear: Int
) : ValueFormatter() {

    private val MONTH_COUNT_PER_YEAR = 12

    override fun getAxisLabel(monthCountFromStart: Float, axis: AxisBase?): String {
        val resultMonthUnnormalized = startMonth + monthCountFromStart.toInt()
        var resultYear = startYear + resultMonthUnnormalized / MONTH_COUNT_PER_YEAR
        var resultMonthNormalized = resultMonthUnnormalized % MONTH_COUNT_PER_YEAR

        if (resultMonthNormalized == 0) {
            resultYear--
            resultMonthNormalized = MONTH_COUNT_PER_YEAR
        }
        return "$resultMonthNormalized/$resultYear"
    }
}