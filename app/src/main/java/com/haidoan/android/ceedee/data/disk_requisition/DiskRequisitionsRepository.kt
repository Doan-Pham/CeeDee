package com.haidoan.android.ceedee.data.disk_requisition

import com.haidoan.android.ceedee.data.Requisition
import kotlinx.coroutines.flow.Flow

private const val TAG = "DiskRequisitionsRepo"

class DiskRequisitionsRepository(
    private val firestoreDataSource: DiskRequisitionsFirestoreDataSource
) {
    fun getRequisitionsStream(): Flow<List<Requisition>> =
        firestoreDataSource.getRequisitionsStream()
}