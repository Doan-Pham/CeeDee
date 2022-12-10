package com.haidoan.android.ceedee.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.io.Serializable

data class DiskStatus (
    @DocumentId
    val id: String = "",
    val name: String = "",
): Serializable {
    override fun toString(): String {
        return name
    }
}