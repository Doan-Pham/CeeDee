package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.haidoan.android.ceedee.data.DiskTitle

class DiskTitlesViewModel(application: Application) : AndroidViewModel(application) {
    private val diskTitlesRepository : DiskTitlesRepository
    private val diskTitleListMutableLiveData : MutableLiveData<ArrayList<DiskTitle>>

    init {
        diskTitlesRepository = DiskTitlesRepository(application)
        diskTitleListMutableLiveData = diskTitlesRepository.getDiskTitleListMutableLiveData()
    }

    fun getDiskTitleListMutableLiveData() : LiveData<ArrayList<DiskTitle>> {
        return diskTitleListMutableLiveData
    }
}