package com.haidoan.android.ceedee.data.customer

import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class CustomerRepository(
    private val firestoreDataSource: CustomerFireStoreDataSource
) {
    fun getCustomersStream(): Flow<List<Customer>> =
        firestoreDataSource.getCustomersStream()

    suspend fun getCustomerByPhone(phoneNumber: String) =
        firestoreDataSource.getCustomerByPhone(phoneNumber)

    suspend fun addCustomer(
        customerName: String?,
        customerAddress: String?,
        customerPhone: String?
    ) =
        flow {
            emit(Response.Loading())
            emit(
                Response.Success(
                    firestoreDataSource.addCustomer(
                        customerName,
                        customerAddress,
                        customerPhone
                    )
                )
            )
        }
            .catch { emit(Response.Failure(it.message.toString())) }

    suspend fun updateCustomer(id: String, address: String?, phone: String?, fullName: String?) =
        flow {
            emit(Response.Loading())
            emit(Response.Success(firestoreDataSource.updateCustomer(id, address, phone, fullName)))
        }
            .catch { emit(Response.Failure(it.message.toString())) }

    fun addOrUpdateCustomer(customer: Customer) = flow {
        emit(Response.Loading())
        emit(Response.Success(firestoreDataSource.addOrUpdateCustomer(customer)))
    }
        .catch { emit(Response.Failure(it.message.toString())) }
}