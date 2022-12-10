package com.haidoan.android.ceedee.ui.disk_screen.disks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskStatusRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.DisksRepository
import kotlinx.coroutines.Dispatchers

class DiskViewModel(application: Application) : AndroidViewModel(application) {
    private val disksRepository: DisksRepository
    private val diskStatusRepository: DiskStatusRepository
    init {
        disksRepository = DisksRepository(application)
        diskStatusRepository = DiskStatusRepository(application)
    }

    fun getDisks() = liveData(Dispatchers.IO) {
        disksRepository.getDisksFromFireStore().collect { response ->
            emit(response)
        }
    }

    fun getDiskStatus() = liveData(Dispatchers.IO) {
        diskStatusRepository.getDiskStatusListFromFireStore().collect { response ->
            emit(response)
        }
    }

}