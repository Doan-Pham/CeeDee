package com.haidoan.android.ceedee.ui.disk_screen.repository

import android.app.Application
import android.util.Log
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

    fun getDisksByStatusFromFireStore(status: String) = flow {
        emit(Response.Loading())
        emit(
            Response.Success(
                queryDisk.whereEqualTo("status",status).get().await().documents.mapNotNull { doc ->
                    doc.toObject(Disk::class.java)
                }
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

    /**
     * Currently, Firestore limits the batch size to 500 for the free version, so the total number of
     * disks shouldn't exceed that limit
     */
    suspend fun importDisks(diskTitlesToImportAndAmount: Map<String, Long>) = flow {
        emit(Response.Loading())
        emit(Response.Success(db.runBatch { batch ->
            for (diskTitleId in diskTitlesToImportAndAmount.keys) {
                val newDiskInfoAsMap = hashMapOf(
                    "diskTitleId" to diskTitleId,
                    "status" to "In Store",
                    "currentRentalId" to ""
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

    suspend fun returnDisksRented(rentalId: String) = flow {
        emit(Response.Loading())
        emit(Response.Success(queryDisk.whereEqualTo("currentRentalId", rentalId).get()
            .addOnSuccessListener { documents ->
                db.runBatch { batch ->
                    for (document in documents) {
                        Log.d(TAG, "${document.id} => ${document.data}")
                        batch.update(
                            queryDisk.document(document.id),
                            mapOf("status" to "In Store", "currentRentalId" to "")
                        )
                    }
                }

            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }.await()
        )
        )
    }.catch { emit(Response.Failure(it.message.toString())) }

    suspend fun rentDisksOfDiskTitleIds(
        rentalId: String,
        diskTitleIdsAndAmount: Map<String, Long>
    ) {

        // This tracker makes sure the amount of disks marked as "Rented" does not exceed the
        // amount to rent for each disk title
        val disksRentedTracker = diskTitleIdsAndAmount.toMutableMap()

        queryDisk.whereIn("diskTitleId", diskTitleIdsAndAmount.keys.toList())
            .whereEqualTo("status", "In Store").get()
            .addOnSuccessListener { documents ->
                db.runBatch { batch ->
                    for (document in documents) {
                        Log.d(
                            TAG,
                            "Called rentDisksOfDiskTitleIds: ${document.id} => ${document.data}"
                        )
                        val currentDocDiskTitleId = document.get("diskTitleId") as String
                        if (disksRentedTracker[currentDocDiskTitleId]!! > 0) {
                            batch.update(
                                queryDisk.document(document.id),
                                mapOf("status" to "Rented", "currentRentalId" to rentalId)
                            )
                            disksRentedTracker[currentDocDiskTitleId] =
                                (disksRentedTracker[currentDocDiskTitleId] ?: 0) - 1
                        }

                    }
                }

            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }
}