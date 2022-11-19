package com.haidoan.android.ceedee.data.disk_requisition

import com.haidoan.android.ceedee.data.Requisition
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

private const val TAG = "DiskRequisitionsRepo"

class DiskRequisitionsRepository(
    private val firestoreDataSource: DiskRequisitionsFirestoreDataSource
) {
    fun getRequisitionsStream(): Flow<List<Requisition>> =
        firestoreDataSource.getRequisitionsStream()

    fun getRequisitionStreamById(requisitionId: String) =
        firestoreDataSource.getRequisitionStreamById(requisitionId)

    suspend fun completeRequisition(requisitionId: String) {
        coroutineScope { launch { firestoreDataSource.completeRequisition(requisitionId) } }
    }
}