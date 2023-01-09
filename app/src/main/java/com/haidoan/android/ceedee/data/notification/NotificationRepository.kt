package com.haidoan.android.ceedee.data.notification

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

private const val TAG = "NotificationRepository"

class NotificationRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var collectionPath: CollectionReference = db.collection("NotificationToken")

    suspend fun addToken(userUid: String, token: String) =
        collectionPath.document(userUid).set(hashMapOf("token" to token)).await()

    suspend fun deleteToken(userUid: String) =
        collectionPath.document(userUid).delete().await()
}