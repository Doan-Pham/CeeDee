package com.haidoan.android.ceedee.data.disk_rental

import android.util.Log
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.Rental
import com.haidoan.android.ceedee.data.Requisition
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsFirestoreDataSource
import com.haidoan.android.ceedee.data.supplier.Supplier
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class DiskRentalRepository(private val firestoreDataSource: DiskRentalFiresoreDataSource) {
    fun getRentalStream(): Flow<List<Rental>> =
        firestoreDataSource.getRentalStream()

    fun getRentalStreamById(rentalId: String) =
        firestoreDataSource.getRentalStreamById(rentalId)

    suspend fun completeRental(rentalId: String) = flow {
        emit(Response.Loading())
        emit(Response.Success(firestoreDataSource.completeRental(rentalId)))
    }
        .catch { emit(Response.Failure(it.message.toString())) }

    suspend fun addRental(
        customerName: String?,
        customerAddress: String?,
        customerPhone: String?,
        diskTitlesToAdd: Map<DiskTitle, Long>
    ) =
        flow {
            emit(Response.Loading())
            emit(
                Response.Success(
                    firestoreDataSource.addRental(
                        customerName,
                        customerAddress,
                        customerPhone,
                        diskTitlesToAdd
                    )
                )
            )
        }
            .catch { emit(Response.Failure(it.message.toString())) }

}