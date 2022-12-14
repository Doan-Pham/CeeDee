package com.haidoan.android.ceedee.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.io.Serializable

data class Disk (
    @DocumentId
    val id: String = "",
    val currentRentalId: String = "",
    val diskTitleId: String = "",
    val importDate: Timestamp = Timestamp.now(),
    val status: String = ""
): Serializable