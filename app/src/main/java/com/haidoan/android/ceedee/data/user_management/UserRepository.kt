package com.haidoan.android.ceedee.data.user_management

import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.User
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class UserRepository(private val firestoreDataSource: UserFirestoreDataSource) {
    fun getUsersStream(): Flow<List<User>> =
        firestoreDataSource.getUsersStream()

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