package com.haidoan.android.ceedee.data.disk_rental

import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class RentalStatusRepository(private val firestoreDataSource: RentalStatusFirestoreDataSource) {
    fun getRentalStatusStream() = flow {
        emit(Response.Loading())
        emit(Response.Success(firestoreDataSource.getRentalStatusStream()))
    }.catch { emit(Response.Failure(it.message.toString())) }

}