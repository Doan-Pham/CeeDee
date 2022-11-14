package com.haidoan.android.ceedee.data

import com.google.firebase.firestore.DocumentId
import java.time.LocalDate

data class Requisition(
    @DocumentId
    val id: String = "",
    val supplierName: String = "",
    val supplierEmail: String = "",
    val diskTitlesToImport: List<DiskTitle> = listOf(),
    val sentDate: LocalDate = LocalDate.now()
)
