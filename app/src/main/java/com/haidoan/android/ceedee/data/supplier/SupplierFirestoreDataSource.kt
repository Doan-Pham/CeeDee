package com.haidoan.android.ceedee.data.supplier

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SupplierFirestoreDataSource {
    private val firestoreDb: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val collectionRef = firestoreDb.collection("Supplier")

    fun getSuppliersStream(): Flow<List<Supplier>> =
        collectionRef.snapshots().map { querySnapshot ->
            querySnapshot.documents.mapNotNull {
                it.toObject(Supplier::class.java)
            }
        }
}