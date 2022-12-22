package com.haidoan.android.ceedee.data.customer

import com.google.firebase.firestore.DocumentId

data class Customer(
    @DocumentId
    val id: String = "",
    val address: String = "",
    val fullName: String = "",
    val phone: String = ""
)