package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.app.Application
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.haidoan.android.ceedee.data.Genre
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class GenreRepository(application: Application) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var queryGenre: CollectionReference = db.collection("Genre")

    init {

    }

    fun getGenresFromFireStore() = flow {
        emit(Response.Loading())
        emit(Response.Success(queryGenre.get().await().documents.mapNotNull { doc ->
            Log.d("TAG", "GET POST SUCCESS")
            doc.toObject(Genre::class.java)
        }))
    }.catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
        }
    }

    fun getGenreByIdFireStore(id: String) = flow {
        emit(Response.Loading())
        emit(
            Response.Success(
                queryGenre.document(id)
                    .get()
                    .await()
                    .toObject(Genre::class.java)
            )
        )
    }.catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
        }
    }

}