package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.ktx.Firebase
import com.haidoan.android.ceedee.data.DiskTitle

class DiskTitlesRepository(private val application: Application) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val diskTitleListMutableLiveData: MutableLiveData<ArrayList<DiskTitle>> = MutableLiveData()

    private val TAG = "FETCH"
    private val collectionPath = "DiskTitle"

    init {

    }

    private fun getDiskTitleFromDocument(document: QueryDocumentSnapshot): DiskTitle {
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
    }

    fun getDiskTitleListMutableLiveData() : MutableLiveData<ArrayList<DiskTitle>> {
        val list = ArrayList<DiskTitle>()
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
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
        diskTitleListMutableLiveData.postValue(list)
        return diskTitleListMutableLiveData
    }
}