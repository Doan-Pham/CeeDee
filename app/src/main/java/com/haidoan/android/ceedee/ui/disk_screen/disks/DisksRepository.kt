package com.haidoan.android.ceedee.ui.disk_screen.disks

import android.app.Application
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.haidoan.android.ceedee.ui.disk_screen.disk_titles.Response
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class DisksRepository(application: Application) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var queryDisk: CollectionReference = db.collection("Disk")

    init {

    }

    fun getDiskAmountInDiskTitlesFromFireStore(diskTitleId: String) = flow {
        emit(Response.Loading())
        emit(
            Response.Success(
                queryDisk.whereEqualTo("diskTitleId", diskTitleId)
                    .count()
                    .get(AggregateSource.SERVER)
                    .await()
            )
        )
    }.catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
        }
    }
}