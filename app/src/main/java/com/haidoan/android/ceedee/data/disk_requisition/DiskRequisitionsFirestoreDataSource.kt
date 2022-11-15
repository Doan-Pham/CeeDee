@file:Suppress("UNCHECKED_CAST")

package com.haidoan.android.ceedee.data.disk_requisition

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.haidoan.android.ceedee.data.Requisition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.ZoneId

private const val TAG = "DiskReqFirestoreDataSrc"

class DiskRequisitionsFirestoreDataSource {
    private val firestoreDb: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getRequisitionsStream(): Flow<List<Requisition>> =
        firestoreDb.collection("Requisition").snapshots().map { querySnapshot ->
            querySnapshot.documents.map {
                Requisition(
                    it.id,
                    it.get("supplierName") as String,
                    it.get("supplierEmail") as String,
                    it.get("diskTitlesToImport") as Map<String, Long>,
                    (it.get("sentDate") as Timestamp).toDate().toInstant()
                        .atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate()
                )
            }
        }

}