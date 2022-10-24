package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

import androidx.lifecycle.liveData
import com.google.firebase.firestore.Query
import com.haidoan.android.ceedee.utils.TypeUtils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch

class DiskTitlesViewModel(application: Application) : AndroidViewModel(application) {
    private val diskTitlesRepository : DiskTitlesRepository

    init {
        diskTitlesRepository = DiskTitlesRepository(application)
    }

    fun getDiskAmountInDiskTitlesFromFireStore(diskTitleId : String) = liveData(Dispatchers.IO) {
        diskTitlesRepository.getDiskAmountInDiskTitlesFromFireStore(diskTitleId).collect { response ->
            emit(response)
        }
    }

 /*   fun getDiskAmountInDiskTitlesFromFireStore(diskTitleId : String) : MutableLiveData<Long> {
        return diskTitlesRepository.getDiskAmountInDiskTitlesFromFireStore(diskTitleId)
    }*/

    fun getDiskTitles() = liveData(Dispatchers.IO) {
        diskTitlesRepository.getDiskTitlesFromFireStore().collect { response ->
            emit(response)
        }
    }

    fun getGenres() = liveData(Dispatchers.IO) {
        diskTitlesRepository.getGenresFromFireStore().collect { response ->
            emit(response)
        }
    }

}