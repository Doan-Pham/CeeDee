package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.haidoan.android.ceedee.data.DiskTitle
import kotlinx.coroutines.Dispatchers

class DiskTitlesViewModel(application: Application) : AndroidViewModel(application) {
    private val diskTitlesRepository : DiskTitlesRepository

    init {
        diskTitlesRepository = DiskTitlesRepository(application)
    }

    fun getDiskTitles() = liveData(Dispatchers.IO) {
        diskTitlesRepository.getDiskTitlesFromFireStore().collect { response ->
            emit(response)
        }
    }

}