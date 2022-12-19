package com.haidoan.android.ceedee.data

import com.google.firebase.firestore.DocumentId

/**
 * Without default values, can't convert Firestore data types to data class
 */
data class UserRole(
    @DocumentId
    val id: String = "NOT_FOUND_ID",
    val name: String = "NOT_FOUND_NAME"
)
