package com.haidoan.android.ceedee.fragmentRentalTabs.Repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.*
import com.haidoan.android.ceedee.data.DiskTitle

class ChooseDiskRepository {
    private val dbf: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val tempList: ArrayList<DiskTitle> = arrayListOf<DiskTitle>()
    private val _diskTileList: ArrayList<DiskTitle> = arrayListOf<DiskTitle>()

    private var inst: ChooseDiskRepository? = null

    fun getInstance(): ChooseDiskRepository {
        return inst ?: synchronized(this) {
            val instance = ChooseDiskRepository()
            inst = instance
            instance
        }
    }
    fun loadUsers(diskTitleList: MutableLiveData<ArrayList<DiskTitle>>) {
        dbf.collection("DiskTitle").addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Log.e("Fire store error", error.message.toString())
                    return
                }
                for (dc: DocumentChange in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        _diskTileList.add(dc.document.toObject(DiskTitle::class.java))
                    }
                }
                for(i in _diskTileList)
                {
                    if(i.diskAmount>0)
                        tempList.add(i)
                }
                diskTitleList.postValue(tempList)
            }
        })
    }
}