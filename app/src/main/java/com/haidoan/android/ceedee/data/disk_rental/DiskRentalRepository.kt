package com.haidoan.android.ceedee.data.disk_rental

import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.Rental
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class DiskRentalRepository(private val firestoreDataSource: DiskRentalFirestoreDataSource) {
    fun getRentalsStream(): Flow<List<Rental>> =
        firestoreDataSource.getRentalsStream()

    fun getRentalStreamById(rentalId: String) =
        firestoreDataSource.getRentalStreamById(rentalId)

    suspend fun completeRental(rentalId: String, totalPayment: Long) = flow {
        emit(Response.Loading())
        emit(Response.Success(firestoreDataSource.completeRental(rentalId, totalPayment)))
    }
        .catch { emit(Response.Failure(it.message.toString())) }

    suspend fun addRental(
        customerName: String?,
        customerAddress: String?,
        customerPhone: String?,
        diskTitlesToAdd: Map<DiskTitle, Long>,
        rentalStatus: String?
    ) =
        flow {
            emit(Response.Loading())
            emit(
                Response.Success(
                    firestoreDataSource.addRental(
                        customerName,
                        customerAddress,
                        customerPhone,
                        diskTitlesToAdd,
                        rentalStatus
                    )
                )
            )
        }
            .catch { emit(Response.Failure(it.message.toString())) }

    suspend fun acceptRentalInRequest(rentalId: String) =
        firestoreDataSource.acceptRentalInRequest(rentalId)

    suspend fun deleteRental(rentalId: String) = firestoreDataSource.deleteRental(rentalId)
}