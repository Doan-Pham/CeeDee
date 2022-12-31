package com.haidoan.android.ceedee.ui.disk_screen.disks

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import com.haidoan.android.ceedee.data.Disk
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskStatusRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.DisksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect

class DiskViewModel(application: Application) : AndroidViewModel(application) {
    private val disksRepository: DisksRepository
    private val diskStatusRepository: DiskStatusRepository
    private val diskTitlesRepository: DiskTitlesRepository

    init {
        disksRepository = DisksRepository(application)
        diskStatusRepository = DiskStatusRepository(application)
        diskTitlesRepository = DiskTitlesRepository(application)
    }

    fun getDiskByDiskStatus(status: String) = liveData(Dispatchers.IO) {
        disksRepository.getDisksByStatusFromFireStore(status).collect { response ->
            emit(response)
        }
    }


    fun updateDiskStatus(disk: Disk, status: String) = liveData(Dispatchers.IO) {
        disksRepository.updateStatusToFireStore(disk.id, status).collect { response ->
            emit(response)
        }
        diskTitlesRepository.updateDiskInStoreAmount(listOf(disk.diskTitleId)).collect()
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