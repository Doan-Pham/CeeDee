package com.haidoan.android.ceedee.data.disk_import

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DiskImportRepository(
    private val firestoreDataSource: DiskImportFirestoreDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun addImport(newImport: Import) = withContext(dispatcher) {
        firestoreDataSource.addImport(newImport)
    }
}