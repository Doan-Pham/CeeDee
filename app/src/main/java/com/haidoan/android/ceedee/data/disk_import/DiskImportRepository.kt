package com.haidoan.android.ceedee.data.disk_import

import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class DiskImportRepository(
    private val firestoreDataSource: DiskImportFirestoreDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun addImport(newImport: Import) =
        flow {
            emit(Response.Loading())
            emit(Response.Success(firestoreDataSource.addImport(newImport)))
        }
            .catch { emit(Response.Failure(it.message.toString())) }

}