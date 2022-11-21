package com.haidoan.android.ceedee.data.supplier

import kotlinx.coroutines.flow.Flow

class SupplierRepository(
    private val firestoreDataSource: SupplierFirestoreDataSource
) {
    fun getSuppliersStream(): Flow<List<Supplier>> =
        firestoreDataSource.getSuppliersStream()
}