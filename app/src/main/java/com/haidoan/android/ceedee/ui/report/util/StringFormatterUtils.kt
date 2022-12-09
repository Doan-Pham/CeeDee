package com.haidoan.android.ceedee.ui.report.util

import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun LocalDate.toFormattedString(): String =
    DateTimeFormatter.ofPattern("dd/MM/yyyy").format(this)

fun Long.toFormattedCurrencyString(): String = " ${DecimalFormat("#,###").format(this)} VND"