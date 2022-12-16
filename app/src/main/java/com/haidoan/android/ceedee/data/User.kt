package com.haidoan.android.ceedee.data

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val id: String = "NOT_FOUND_UID",
    val username: String = "NOT_FOUND_USERNAME",
    val password: String = "NOT_FOUND_PASSWORD",
    val role: Long = -1L,
)

const val USER_ROLE_CUSTOMER = 1L
const val USER_ROLE_EMPLOYEE = 2L
const val USER_ROLE_MANAGER = 3L

