package com.haidoan.android.ceedee.ui.disk_screen.repository

import android.app.Application
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.haidoan.android.ceedee.data.DiskStatus
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class DiskStatusRepository(private val application: Application) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var queryDiskStatus: CollectionReference = db.collection("DiskStatus")

    init {

    }

    companion object {
        const val defaultDiskStatus = "default_disk_status"
    }

    fun getDiskStatusListFromFireStore() = flow {
        emit(Response.Loading())
        emit(
            Response.Success(
                queryDiskStatus.get().await().documents.mapNotNull { doc ->
                    doc.toObject(DiskStatus::class.java)
                }
            )
        )
    }.catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
        }
    }

}