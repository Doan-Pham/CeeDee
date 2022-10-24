package com.haidoan.android.ceedee.ui.report.util

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.time.LocalDate

class MonthYearXAxisValueFormatter(
    private val starTime: LocalDate,
) : ValueFormatter() {

    private val MONTH_COUNT_PER_YEAR = 12

    override fun getAxisLabel(monthCountFromStart: Float, axis: AxisBase?): String {
        val resultMonthUnnormalized = starTime.monthValue + monthCountFromStart.toInt()
        var resultYear = starTime.year + resultMonthUnnormalized / MONTH_COUNT_PER_YEAR
        var resultMonthNormalized = resultMonthUnnormalized % MONTH_COUNT_PER_YEAR

        if (resultMonthNormalized == 0) {
            resultYear--
            resultMonthNormalized = MONTH_COUNT_PER_YEAR
        }
        return "$resultMonthNormalized/$resultYear"
    }
}