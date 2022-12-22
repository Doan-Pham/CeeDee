package com.haidoan.android.ceedee.data

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId
    val id: String = "NOT_FOUND_UID",
    val username: String = "NOT_FOUND_USERNAME",
    val password: String = "NOT_FOUND_PASSWORD",
    val role: String = "NOT_FOUND_ROLE",
)

const val USER_ROLE_CUSTOMER = "Customer"
const val USER_ROLE_EMPLOYEE = "Employee"
const val USER_ROLE_MANAGER = "Manager"

