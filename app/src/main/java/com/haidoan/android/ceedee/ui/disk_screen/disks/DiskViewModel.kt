package com.haidoan.android.ceedee.ui.disk_screen.disks

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.liveData
import com.haidoan.android.ceedee.data.Disk
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskStatusRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.DisksRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect

private const val TAG = "DiskViewModel"

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
            if (response is Response.Success) {
                diskTitlesRepository.getDiskTitlesByListOfId(response.data.map { it.diskTitleId })
                    .collect { diskTitles ->
                        emit(Response.Success(response.data.map {
                            DiskAndSomeInfo(
                                disk = it,
                                coverImage = diskTitles.first { diskTitle ->
                                    diskTitle.id == it.diskTitleId
                                }.coverImageUrl,
                                diskTitle = diskTitles.first { diskTitle ->
                                    diskTitle.id == it.diskTitleId
                                }.name
                            )
                        }))
                    }
            } else
                emit(response)
        }
    }


    fun updateDiskStatus(disk: Disk, status: String) = liveData(Dispatchers.IO) {
        disksRepository.updateStatusToFireStore(disk.id, status).collect { response ->
            emit(response)
            diskTitlesRepository.updateDiskInStoreAmount(listOf(disk.diskTitleId)).collect()
        }
    }

    fun getDisks() = liveData(Dispatchers.IO) {
        disksRepository.getDisksFromFireStore().collect { response ->
            if (response is Response.Success) {
                diskTitlesRepository.getDiskTitlesByListOfId(response.data.map { it.diskTitleId })
                    .collect { diskTitles ->
                        Log.d(TAG, "getDisks() - diskTitles: $diskTitles")
                        emit(Response.Success(response.data.map {
                            Log.d(TAG, "currentDisk - diskid: ${it.id}")
                            Log.d(TAG, "currentDisk - disk disktitleid: ${it.diskTitleId}")
                            Log.d(
                                TAG, "currentDisk - disktitle: ${
                                    diskTitles.first { diskTitle ->
                                        diskTitle.id == it.diskTitleId
                                    }.id
                                }"
                            )
                            DiskAndSomeInfo(
                                disk = it,
                                coverImage = diskTitles.first { diskTitle ->
                                    diskTitle.id == it.diskTitleId
                                }.coverImageUrl,
                                diskTitle = diskTitles.first { diskTitle ->
                                    diskTitle.id == it.diskTitleId
                                }.name
                            )
                        }))
                    }
            } else
                emit(response)
        }
    }

    fun getDiskStatus() = liveData(Dispatchers.IO) {
        diskStatusRepository.getDiskStatusListFromFireStore().collect { response ->
            emit(response)
        }
    }

}

data class DiskAndSomeInfo(val disk: Disk, val coverImage: String, val diskTitle: String)