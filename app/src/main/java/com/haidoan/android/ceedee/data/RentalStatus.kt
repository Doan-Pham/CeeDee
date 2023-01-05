package com.haidoan.android.ceedee.data

import com.google.firebase.firestore.DocumentId

data class RentalStatus(
    @DocumentId
    val id: String = "NOT_FOUND_ID",
    val name: String = "NOT_FOUND_NAME"
)
