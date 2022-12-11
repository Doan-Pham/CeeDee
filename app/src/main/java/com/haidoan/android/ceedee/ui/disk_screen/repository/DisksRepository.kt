package com.haidoan.android.ceedee.ui.disk_screen.repository

import android.app.Application
import com.google.firebase.Timestamp
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.haidoan.android.ceedee.data.Disk
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

private const val TAG = "DisksRepository"

class DisksRepository(private val application: Application) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var queryDisk: CollectionReference = db.collection("Disk")

    init {

    }

    fun updateStatusToFireStore(
        id: String,
        status: String
    ) = flow {
        emit(Response.Loading())
        emit(
            Response.Success(
                queryDisk.document(id).update(
                    "status", status
                ).await()
            )
        )
    }.catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
        }
    }

    fun getDisksFromFireStore() = flow {
        emit(Response.Loading())
        emit(
            Response.Success(
                queryDisk.get().await().documents.mapNotNull { doc ->
                    doc.toObject(Disk::class.java)
                }
            )
        )
    }.catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
        }
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

    /**
     * Currently, Firestore limits the batch size to 500 for the free version, so the total number of
     * disks shouldn't exceed that limit
     */
    suspend fun addMultipleDisks(diskTitlesToImportAndAmount: Map<String, Long>) = flow {
        emit(Response.Loading())
        emit(Response.Success(db.runBatch { batch ->
            for (diskTitleId in diskTitlesToImportAndAmount.keys) {
                val newDiskInfoAsMap = hashMapOf(
                    "diskTitleId" to diskTitleId,
                    "status" to "In Store",
                    "totalRentalCount" to 0,
                    "importDate" to Timestamp.now()
                )
                val diskAmountToImport: Long = diskTitlesToImportAndAmount[diskTitleId] ?: 0

                for (i in 1..diskAmountToImport) {
                    // Have to write this because there is not batch.add() method
                    val newDiskDocumentRef = db.collection("Disk").document()
                    batch.set(newDiskDocumentRef, newDiskInfoAsMap)
                }
            }
        }.await()))
    }
        .catch { emit(Response.Failure(it.message.toString())) }
}