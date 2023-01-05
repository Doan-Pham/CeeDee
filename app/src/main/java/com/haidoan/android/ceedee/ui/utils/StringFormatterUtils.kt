package com.haidoan.android.ceedee.ui.utils

import com.google.firebase.Timestamp
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun LocalDate.toFormattedString(): String =
    DateTimeFormatter.ofPattern("dd/MM/yyyy").format(this)

fun Long.toFormattedCurrencyString(): String = " ${DecimalFormat("#,###").format(this)} VND"

fun Timestamp.toFormattedMonthYearString(): String {
    val zoneId = ZoneId.of("Asia/Ho_Chi_Minh")
    val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/yyyy")
    val localDate: LocalDate? = this.toDate().toInstant()?.atZone(zoneId)?.toLocalDate()
    return dtf.format(localDate)
}

fun String.toPhoneNumberWithoutCountryCode(): String {
    val phoneInstance = PhoneNumberUtil.getInstance()
    val phoneNumber = phoneInstance.parse(this, "VN")
    return ("0" + phoneNumber?.nationalNumber?.toString())
}
