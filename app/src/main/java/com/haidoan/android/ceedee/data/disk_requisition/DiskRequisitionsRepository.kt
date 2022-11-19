package com.haidoan.android.ceedee.data.disk_requisition

import com.haidoan.android.ceedee.data.Requisition
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

private const val TAG = "DiskRequisitionsRepo"

class DiskRequisitionsRepository(
    private val firestoreDataSource: DiskRequisitionsFirestoreDataSource
) {
    fun getRequisitionsStream(): Flow<List<Requisition>> =
        firestoreDataSource.getRequisitionsStream()

    fun getRequisitionStreamById(requisitionId: String) =
        firestoreDataSource.getRequisitionStreamById(requisitionId)

    suspend fun completeRequisition(requisitionId: String) = flow {
        emit(Response.Loading())
        emit(Response.Success(firestoreDataSource.completeRequisition(requisitionId)))
    }
        .catch { emit(Response.Failure(it.message.toString())) }
}