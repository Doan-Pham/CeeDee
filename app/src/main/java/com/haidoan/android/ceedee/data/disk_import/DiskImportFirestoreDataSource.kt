package com.haidoan.android.ceedee.data.disk_import

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DiskImportFirestoreDataSource {
    private val firestoreDb: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun addImport(newImport: Import): DocumentReference? {
        val newImportAsMap = hashMapOf(
            "supplierName" to newImport.supplierName,
            "date" to Timestamp.now(),
            "totalPayment" to newImport.totalPayment
        )
        return firestoreDb.collection("Import").add(newImportAsMap).await()
    }
}