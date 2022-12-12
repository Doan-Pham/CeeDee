package com.haidoan.android.ceedee.data

import com.google.firebase.Timestamp

data class Rental(
    var id : String? = null,
    var customerName: String? = null,
    var customerPhone: String? = null,
    var customerAddress: String? = null,
    var map: Map<String,Long> = mapOf(),
    var dueDate: Timestamp? = null,
    var rentDate: Timestamp? = null,
    var returnDate: Timestamp? = null,
    var rentalStatus: String? = null,
    var totalPayment: Float? = null,
)