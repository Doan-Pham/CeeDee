package com.haidoan.android.ceedee.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Rental(
    @DocumentId
    var id: String = "NOT_FOUND_ID",
    var customerName: String = "NOT_FOUND_CUSTOMER_NAME",
    var customerPhone: String = "NOT_FOUND_CUSTOMER_PHONE",
    var customerAddress: String = "NOT_FOUND_CUSTOMER_ADDRESS",
    var diskTitlesToAdd: Map<String, Long> = mapOf(),
    var dueDate: Timestamp? = null,
    var rentDate: Timestamp? = null,
    var returnDate: Timestamp? = null,
    var rentalStatus: String? = null,
    var totalPayment: Long? = null,
)