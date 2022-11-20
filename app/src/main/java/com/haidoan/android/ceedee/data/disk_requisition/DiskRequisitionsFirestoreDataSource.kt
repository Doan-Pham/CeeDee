@file:Suppress("UNCHECKED_CAST")

package com.haidoan.android.ceedee.data.disk_requisition

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.Requisition
import com.haidoan.android.ceedee.data.supplier.Supplier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.time.ZoneId

private const val TAG = "DiskReqFirestoreDataSrc"

class DiskRequisitionsFirestoreDataSource {
    private val firestoreDb: FirebaseFirestore = FirebaseFirestore.getInstance()

    // For some reason, the snapshots() API is very buggy and sometimes causes app crash
    fun getRequisitionsStream(): Flow<List<Requisition>> = flow {
        emit(firestoreDb.collection("Requisition").get().await().documents.mapNotNull {
            Requisition(
                it.id,
                it.get("supplierName") as String,
                it.get("supplierEmail") as String,
                it.get("diskTitlesToImport") as Map<String, Long>,
                (it.get("sentDate") as Timestamp).toDate().toInstant()
                    .atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate(),
                // Naming a Firestore field as "status" somehow causes the app to crash
                it.get("requisitionStatus") as String
            )
        })

    }

    fun getRequisitionStreamById(requisitionId: String): Flow<Requisition> = flow {
        val result = firestoreDb.collection("Requisition").document(requisitionId).get().await()
        emit(
            Requisition(
                result.id,
                result.get("supplierName") as String,
                result.get("supplierEmail") as String,
                result.get("diskTitlesToImport") as Map<String, Long>,
                (result.get("sentDate") as Timestamp).toDate().toInstant()
                    .atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate(),
                // Naming a Firestore field as "status" somehow causes the app to crash
                result.get("requisitionStatus") as String
            )
        )
    }

    suspend fun completeRequisition(requisitionId: String) =
        firestoreDb.collection("Requisition").document(requisitionId)
            .update("requisitionStatus", "Completed").await()

    suspend fun addRequisition(
        supplier: Supplier?,
        diskTitlesToImport: Map<DiskTitle, Long>
    ): DocumentReference? {

        val diskTitleIdToAmountMap = diskTitlesToImport.mapKeys { it.key.id }
        val newRequisitionAsMap = hashMapOf(
            "supplierName" to (supplier?.name ?: ""),
            "supplierEmail" to (supplier?.email ?: ""),
            "sentDate" to Timestamp.now(),
            "requisitionStatus" to "Pending",
            "diskTitlesToImport" to diskTitleIdToAmountMap
        )
        Log.d(TAG, "diskTitleIdToAmountMap : $diskTitleIdToAmountMap")
        Log.d(TAG, "addRequisition : $newRequisitionAsMap")
        return firestoreDb.collection("Requisition").add(newRequisitionAsMap).await()
    }

}