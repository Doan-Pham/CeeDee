package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.AggregateSource

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.Genre
import com.haidoan.android.ceedee.utils.TypeUtils

import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

import kotlinx.coroutines.tasks.await


class DiskTitlesRepository(private val application: Application) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var queryDiskTitle: CollectionReference = db.collection("DiskTitle")

    init {

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