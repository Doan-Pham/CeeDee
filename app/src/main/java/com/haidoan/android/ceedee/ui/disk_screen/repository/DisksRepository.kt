package com.haidoan.android.ceedee.ui.disk_screen.repository

import android.app.Application
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

private const val TAG = "DisksRepository"

class DisksRepository(private val application: Application) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var queryDisk: CollectionReference = db.collection("Disk")

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
            }.await())
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