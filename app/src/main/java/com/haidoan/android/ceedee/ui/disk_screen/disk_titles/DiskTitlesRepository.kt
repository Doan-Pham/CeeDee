package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.app.Application

import android.util.Log

import androidx.lifecycle.MutableLiveData

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

import com.haidoan.android.ceedee.data.DiskTitle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

import kotlinx.coroutines.tasks.await

class DiskTitlesRepository(private val application: Application) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var queryDiskTitle: CollectionReference = db.collection("DiskTitle")

    init {

    }

    fun getDiskTitlesFromFireStore() = flow {
        emit(Response.Loading())
        emit(Response.Success(queryDiskTitle.get().await().documents.mapNotNull { doc ->
            Log.d("TAG", "GET POST SUCCESS")
            doc.toObject(DiskTitle::class.java)
        }))
    }. catch { error ->
        error.message?.let { errorMessage ->
            emit(Response.Failure(errorMessage))
        }
    }

  /*  private fun getDiskTitleFromDocument(document: QueryDocumentSnapshot): DiskTitle {
        val id = document.id
        val genreId = document.data["genreId"]
        val name = document.data["name"]
        val author = document.data["author"]
        val description = document.data["description"]
        val imgUrl = document.data["coverImageUrl"]
        return DiskTitle(
            id = id,
            genreId = genreId as String,
            name = name as String,
            author = author as String,
            description = description as String,
            coverImageURL = imgUrl as String
        )
    }*/

/*
    fun getDiskTitleListMutableLiveData() : MutableLiveData<ArrayList<DiskTitle>> {
        val list = ArrayList<DiskTitle>()
        Toast.makeText(application, "Loading...",Toast.LENGTH_SHORT).show()

        db.collection(collectionPath)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "${document.id} => ${document.data["name"]}")
                    //val d = document.toObject(DiskTitle::class.java)
                    // Log.d(TAG, d.name +" + "  + d.author)
                    //diskTitleList.add(d)
                    val diskTitle=getDiskTitleFromDocument(document)
                    list.add(diskTitle)
                    Toast.makeText(application, "Success!!!",Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
        diskTitleListMutableLiveData.postValue(list)
        return diskTitleListMutableLiveData
    }*/
}