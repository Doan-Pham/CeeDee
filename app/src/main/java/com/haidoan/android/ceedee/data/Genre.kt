package com.haidoan.android.ceedee.data

import com.google.firebase.firestore.DocumentId

data class Genre(
    @DocumentId
    val id: String = "",
    val name: String = ""):java.io.Serializable