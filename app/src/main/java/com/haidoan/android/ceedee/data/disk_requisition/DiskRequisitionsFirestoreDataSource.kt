@file:Suppress("UNCHECKED_CAST")

package com.haidoan.android.ceedee.data.disk_requisition

import com.google.firebase.firestore.FirebaseFirestore
import com.haidoan.android.ceedee.data.Requisition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class DiskRequisitionsFirestoreDataSource {
    private val firestoreDb: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun getAllRequisitions(): Flow<List<Requisition>> = flow {
        emit(firestoreDb.collection("Requisition").get().await().documents.map {
            Requisition(
//                it.id,
//                it.get("supplierName") as String,
//                it.get("supplierEmail") as String,
//                it.get("diskTitlesToImport") as Map<String, Long>,
//                (it.get("sentDate") as Timestamp).toDate().toInstant()
//                    .atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate()
            )

        })

    }

}