package com.haidoan.android.ceedee.data

import com.google.firebase.firestore.DocumentId
import java.io.Serializable

data class DiskTitle(
    @DocumentId
    val id: String = "",
    val genreId: String = "",
    val name: String = "",
    val author: String = "",
    val coverImageUrl: String = "",
    val description: String = "",
    val diskAmount: Long = 0,
    val diskInStoreAmount: Long = 0,
) : Serializable