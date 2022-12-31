package com.haidoan.android.ceedee.ui.disk_screen.repository

import android.app.Application
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

private const val TAG = "DiskTitlesRepository"

class DiskTitlesRepository(private val application: Application) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var queryDiskTitle: CollectionReference = db.collection("DiskTitle")
    private val storageReference = FirebaseStorage.getInstance().reference

    init {

    }

    fun deleteDiskTitleFromFireStore(id: String) = flow {
        emit(Response.Loading())
        emit(
            Response.Success(
                queryDiskTitle.document(id).delete()
            )
        )
    }.catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
        }
    }

    fun getDiskTitleFilterByGenreIdFromFireStore(id: String) = flow {
        emit(Response.Loading())
        emit(
            Response.Success(queryDiskTitle
                .whereEqualTo("genreId", id)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    Log.d("TAG", "GET POST SUCCESS")
                    doc.toObject(DiskTitle::class.java)
                })
        )
    }.catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
        }
    }

    fun getDiskTitlesFromFireStore() = flow {
        emit(Response.Loading())
        emit(Response.Success(queryDiskTitle.get().await().documents.mapNotNull { doc ->
            Log.d("TAG", "GET POST SUCCESS")
            doc.toObject(DiskTitle::class.java)
        }))
    }.catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
        }
    }

    fun getDiskTitlesAvailableInStore() = flow {
        emit(queryDiskTitle.whereGreaterThan("diskAmount", 0).get()
            .await().documents.mapNotNull { it.toObject(DiskTitle::class.java) })
    }


    //  This won't work if listOfId has more than 10 elements due to a limit of Firestore
//TODO: Modify the method to allow for more than 10 disk titles
    fun getDiskTitlesByListOfId(listOfId: List<String>) = flow {
        emit(
            queryDiskTitle.whereIn(FieldPath.documentId(), listOfId).get()
                .await().documents.mapNotNull {
                    it.toObject(DiskTitle::class.java)
                })
    }


    fun addDiskTitleToFireStore(
        author: String,
        coverImageUrl: String,
        description: String,
        genreId: String,
        name: String
    ) = flow {
        val hash = hashMapOf(
            "author" to author,
            "coverImageUrl" to coverImageUrl,
            "description" to description,
            "genreId" to genreId,
            "diskAmount" to 0,
            "totalRentalAmount" to 0,
            "name" to name
        )
        emit(Response.Loading())
        emit(
            Response.Success(
                queryDiskTitle.add(hash).await()
            )
        )
    }.catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
        }
    }

    fun updateDiskTitleToFireStore(
        id: String,
        author: String,
        coverImageUrl: String,
        description: String,
        genreId: String,
        name: String
    ) = flow {

        emit(Response.Loading())
        emit(
            Response.Success(
                queryDiskTitle.document(id).update(
                    "author", author,
                    "coverImageUrl", coverImageUrl,
                    "description", description,
                    "genreId", genreId,
                    "name", name
                ).await()
            )
        )
    }.catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
        }
    }

    fun addImageToFireStore(
        filePath: Uri?,
        name: String?
    ) = flow {
        val ref = storageReference
            .child("disk_titles_img/$name")
        emit(Response.Loading())
        emit(
            Response.Success(
                ref
                    .putFile(filePath!!)
                    .continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        return@continueWithTask ref.downloadUrl
                    }
                    .await()
            )
        )
    }.catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
        }
    }

    fun updateDiskAmount(diskTitleIdsAndAmount: Map<String, Long>) = flow {
        emit(Response.Loading())
        getDiskTitlesByListOfId(diskTitleIdsAndAmount.keys.toList()).collect { diskTitles ->
            emit(
                Response.Success(
                    db.runBatch { batch ->
                        for (diskTitle in diskTitles) {
                            Log.d(TAG, "${diskTitle.id} => $diskTitle")
                            batch.update(
                                queryDiskTitle.document(diskTitle.id),
                                "diskAmount",
                                FieldValue.increment(diskTitleIdsAndAmount[diskTitle.id] ?: 0L)
                            )
                        }
                    }.await()
                )
            )
        }

    }.catch { emit(Response.Failure(it.message.toString())) }

}