package com.haidoan.android.ceedee.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

data class DiskTitle(
    @DocumentId
    val id: String = "",
    val genreId: String = "",
    val name: String = "",
    val author: String = "",
    val coverImageUrl: String = "",
    val description: String = ""
)