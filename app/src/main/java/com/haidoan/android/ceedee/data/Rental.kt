package com.haidoan.android.ceedee.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Rental(
    @DocumentId
    var id: String? = null,
    var customerName: String? = null,
    var customerPhone: String? = null,
    var customerAddress: String? = null,
    var diskTitlesRentedAndAmount: Map<String, Long> = mapOf(),
    var dueDate: Timestamp? = null,
    var rentDate: Timestamp? = null,
    var returnDate: Timestamp? = null,
    var rentalStatus: String? = null,
    var totalPayment: Float? = null,
)