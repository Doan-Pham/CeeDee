package com.haidoan.android.ceedee.fragmentRentalTabs

import com.google.firebase.Timestamp

data class Rental(
    var customerId: String? = null,
    var dueDate: Timestamp? = null,
    var rentDate: Timestamp? = null,
    var returnDate: Timestamp? = null,
    var status: String? = null,
    var totalPayment: Float? = null
)