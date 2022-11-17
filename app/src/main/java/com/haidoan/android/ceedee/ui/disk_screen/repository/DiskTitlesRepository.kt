package com.haidoan.android.ceedee.ui.disk_screen.repository

import android.app.Application
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.snapshots
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import com.haidoan.android.ceedee.ui.disk_screen.utils.TypeUtils
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.tasks.await

class DiskTitlesRepository(private val application: Application) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var queryDiskTitle: CollectionReference = db.collection("DiskTitle")

    init {

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

    fun getDiskTitlesSortByNameFromFireStore(type: TypeUtils.SORT_BY_NAME) = flow {
        emit(Response.Loading())
        emit(
            Response.Success(queryDiskTitle
                .orderBy(
                    "name", when (type) {
                        TypeUtils.SORT_BY_NAME.Ascending -> {
                            Query.Direction.ASCENDING
                        }
                        TypeUtils.SORT_BY_NAME.Descending -> {
                            Query.Direction.DESCENDING
                        }
                    }
                )
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

    //  This won't work if listOfId has more than 10 elements due to a limit of Firestore
    //TODO: Modify the method to allow for more than 10 disk titles
    fun getDiskTitlesByListOfId(listOfId: List<String>) =
        queryDiskTitle.whereIn(FieldPath.documentId(), listOfId).snapshots().mapNotNull {
            it.toObjects(DiskTitle::class.java)
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
}