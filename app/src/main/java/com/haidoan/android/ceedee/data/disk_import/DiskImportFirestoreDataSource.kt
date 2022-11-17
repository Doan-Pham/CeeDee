package com.haidoan.android.ceedee.data.disk_import

import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.tasks.await

class DiskImportFirestoreDataSource {
    private val firestoreDb: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun addImport(newImport: Import): Response.Success<Task<DocumentSnapshot>> {
        val newImportAsMap = hashMapOf(
            "supplierName" to newImport.supplierName,
            "date" to Timestamp.now(),
            "totalPayment" to newImport.totalPayment
        )
        return Response.Success(firestoreDb.collection("Import").add(newImportAsMap).await().get())
    }
}