package com.haidoan.android.ceedee.data.disk_import

import java.time.LocalDateTime

data class Import(
    val requisitionId: String,
    val date: LocalDateTime = LocalDateTime.now(),
    val totalPayment: Long
)
