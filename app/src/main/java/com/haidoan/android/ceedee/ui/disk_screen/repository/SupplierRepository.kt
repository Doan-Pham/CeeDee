package com.haidoan.android.ceedee.ui.disk_screen.repository

import android.app.Application
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlin.collections.HashMap

class SupplierRepository(private val application: Application) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var querySupplier: CollectionReference = db.collection("Supplier")

    init {

    }

    fun addSupplierToFireStore(supplier: HashMap<String, String>) = flow {
        emit(Response.Loading())
        emit(
            Response.Success(querySupplier.add(supplier).await())
        )
    }.catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
        }
    }

}