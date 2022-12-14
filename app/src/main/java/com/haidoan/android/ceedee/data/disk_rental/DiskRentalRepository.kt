package com.haidoan.android.ceedee.data.disk_rental

import android.util.Log
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.Rental
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

private const val TAG = "DiskRentalRepository"

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

    suspend fun getRentalsByCustomerPhoneStream(customerPhone: String) =
        firestoreDataSource.getRentalsByCustomerPhoneStream(customerPhone)
            .catch {
                Log.e(
                    TAG,
                    "getRentalsByCustomerPhoneStream() - an error has occurred: ${it.message}"
                )
                emit(listOf())
            }

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

    suspend fun startAcceptedRental(rentalId: String) =
        firestoreDataSource.startAcceptedRental(rentalId)

    suspend fun deleteRental(rentalId: String) = firestoreDataSource.deleteRental(rentalId)
}