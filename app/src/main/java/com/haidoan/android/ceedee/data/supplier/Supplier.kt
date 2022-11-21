package com.haidoan.android.ceedee.data.supplier

import com.google.firebase.firestore.DocumentId

data class Supplier(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val email: String = "",
)
