package com.haidoan.android.ceedee.data

import com.google.firebase.firestore.DocumentId
import java.time.LocalDateTime

data class Requisition(
    @DocumentId
    val id: String = "",
    val supplierName: String = "",
    val supplierEmail: String = "",
    val diskTitlesToImport: Map<String, Long> = mapOf(),
    val sentDate: LocalDateTime = LocalDateTime.now(),
    val requisitionStatus: String = ""
)
