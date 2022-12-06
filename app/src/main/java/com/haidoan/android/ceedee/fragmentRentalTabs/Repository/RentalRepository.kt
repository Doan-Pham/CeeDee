package com.haidoan.android.ceedee.fragmentRentalTabs.Repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.*
import com.haidoan.android.ceedee.data.Rental

class RentalRepository {

    private val dbf: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val tempList: ArrayList<Rental> = arrayListOf<Rental>()
    private val _rentalList: ArrayList<Rental> = arrayListOf<Rental>()
    @Volatile
    private var inst: RentalRepository? = null

    fun getInstance(): RentalRepository {
        return inst ?: synchronized(this) {
            val instance = RentalRepository()
            inst = instance
            instance
        }
    }
    fun loadUsers(rentalList: MutableLiveData<ArrayList<Rental>>, s: String) {
        dbf.collection("Rental").addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Log.e("Fire store error", error.message.toString())
                    return
                }
                for (dc: DocumentChange in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        _rentalList.add(dc.document.toObject(Rental::class.java))
                    }
                }
                if (s == "All") {
                    rentalList.postValue(_rentalList)
                    return
                } else {
                    _rentalList.filterTo(tempList, { it.rentalStatus == s })
                    rentalList.postValue(tempList)
                }
            }

        })
    }

}