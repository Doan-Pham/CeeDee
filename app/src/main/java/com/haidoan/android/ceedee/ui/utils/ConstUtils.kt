package com.haidoan.android.ceedee.ui.utils

import android.Manifest
import android.graphics.Color

// Unit of measurement is pt, 1 pt = 1/72 inch
val STANDARD_REPORT_PAGE_WIDTH = 595
val STANDARD_REPORT_PAGE_HEIGHT = 842

// Unit of measurement is pt, 1 pt = 1/72 inch
val BILL_PAGE_WIDTH = 400
val BILL_PAGE_HEIGHT = 500

val PERMISSIONS = arrayOf(
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
    Manifest.permission.READ_EXTERNAL_STORAGE
)

val CHART_COLOR_FIRST = Color.rgb(228, 86, 33)
val CHART_COLOR_SECOND = Color.rgb(251, 173, 86)
val CHART_COLOR_THIRD = Color.rgb(160, 215, 113)
val CHART_COLOR_FOURTH = Color.rgb(115, 176, 215)
val CHART_COLOR_FIFTH = Color.rgb(50, 50, 50)
val CHART_COLOR_SIXTH = Color.rgb(100, 100, 100)
val CHART_COLOR_SEVENTH = Color.rgb(150, 150, 150)
val CHART_COLOR_EIGHTH = Color.rgb(200, 200, 200)
val CHART_COLOR_NINE = Color.rgb(25, 25, 25)
val CHART_COLOR_TEN = Color.rgb(0, 0, 0)