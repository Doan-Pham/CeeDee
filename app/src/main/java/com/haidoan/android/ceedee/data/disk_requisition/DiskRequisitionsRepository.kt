package com.haidoan.android.ceedee.data.disk_requisition

private const val TAG = "DiskRequisitionsRepo"

class DiskRequisitionsRepository(
    private val firestoreDataSource: DiskRequisitionsFirestoreDataSource
) {
    suspend fun getAllRequisitions() = firestoreDataSource.getAllRequisitions()
}