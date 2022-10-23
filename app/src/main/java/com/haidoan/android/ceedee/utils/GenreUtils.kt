package com.haidoan.android.ceedee.utils

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.liveData
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.Genre
import com.haidoan.android.ceedee.ui.disk_screen.disk_titles.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class GenreUtils {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        private var queryGenre: CollectionReference = db.collection("Genre")

        /**
         *  Put inside runBlocking{} to run in normal function
         * */
        suspend fun getGenreById(id: String): Genre {
            return queryGenre
                .document(id)
                .get()
                .await()
                .toObject(Genre::class.java)!!
        }


    }
}