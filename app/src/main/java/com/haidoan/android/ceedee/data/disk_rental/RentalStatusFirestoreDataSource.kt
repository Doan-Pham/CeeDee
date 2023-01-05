package com.haidoan.android.ceedee.data.disk_rental

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.haidoan.android.ceedee.data.RentalStatus
import kotlinx.coroutines.flow.mapNotNull

class RentalStatusFirestoreDataSource {
    private val firestoreDb: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val collectionPath = firestoreDb.collection("RentalStatus")

    fun getRentalStatusStream() =
        collectionPath.snapshots().mapNotNull { querySnapshot ->
            querySnapshot.documents.mapNotNull {
                it.toObject(RentalStatus::class.java)
            }
        }

}
