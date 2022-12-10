package com.haidoan.android.ceedee.ui.disk_screen.disk_import

import android.util.Log
import androidx.lifecycle.*
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.Requisition
import com.haidoan.android.ceedee.data.disk_import.DiskImportRepository
import com.haidoan.android.ceedee.data.disk_import.Import
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.DisksRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

// TODO: Implement the Repository
class DiskImportViewModel(
    private val diskRequisitionsRepository: DiskRequisitionsRepository,
    private val diskTitlesRepository: DiskTitlesRepository,
    private val diskImportRepository: DiskImportRepository,
    private val disksRepository: DisksRepository
) :
    ViewModel() {
    private val _requisitionId = MutableLiveData("")

    private val _currentRequisition = _requisitionId.switchMap { requisitionId ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            diskRequisitionsRepository.getRequisitionStreamById(requisitionId).collect {
                emit(it)
            }
        }
    }
    val currentRequisition: LiveData<Requisition>
        get() = _currentRequisition

    private val _disksToImport = _currentRequisition.switchMap { requisition ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            diskTitlesRepository.getDiskTitlesByListOfId(requisition.diskTitlesToImport.keys.toList())
                .collect { diskTitlesList ->
                    if (diskTitlesList.isEmpty()) return@collect
                    val diskTitlesToImportMap = mutableMapOf<DiskTitle, Long>()
                    for (key in requisition.diskTitlesToImport.keys) {
                        diskTitlesToImportMap[diskTitlesList.first { it.id == key }] =
                            requisition.diskTitlesToImport[key] ?: 1
                    }
                    emit(diskTitlesToImportMap.toMap())
                }
        }
    }

    val disksToImport: LiveData<Map<DiskTitle, Long>>
        get() = _disksToImport

    fun setCurrentRequisitionId(requisitionId: String) {
        _requisitionId.value = requisitionId
    }

    fun addNewImport(
        newImport: Import,
        diskTitlesToImportAndAmount: Map<String, Long>,
        requisitionId: String
    ) = liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
        coroutineScope {
            val tasks = listOf(
                async { diskImportRepository.addImport(newImport) },
                async { disksRepository.addMultipleDisks(diskTitlesToImportAndAmount) },
                async { diskRequisitionsRepository.completeRequisition(requisitionId) }
            )
            val taskResponses = tasks.awaitAll()
            val taskResults = mutableListOf<Response<Any?>>()
            for (response in taskResponses) {
                response.collect { result ->
                    Log.d(
                        "DiskImportViewModel",
                        "taskResponses.indexOf(result)" + taskResponses.indexOf(response)
                    )
                    taskResults.add(taskResponses.indexOf(response), result)

                    // For some reason, using the set() method and [] cause app crash, so this is
                    // a workaround for replacing old value
                    if (taskResults.size > taskResponses.indexOf(response) + 1)
                        taskResults.removeAt(taskResponses.indexOf(response) + 1)
                    Log.d("DiskImportViewModel", "taskResults " + taskResults.toString())

                    if (taskResults.size < taskResponses.size) return@collect

                    if (taskResults.all { it is Response.Success }) emit(Response.Success(1))
                    else if (taskResults.any { it is Response.Loading }) emit(Response.Loading<Int>())
                    else if (taskResults.any { it is Response.Failure }) emit(Response.Failure("An error has occurred importing new disks"))
                }
            }
        }

    }

    class Factory(
        private val diskRequisitionsRepository: DiskRequisitionsRepository,
        private val diskTitlesRepository: DiskTitlesRepository,
        private val diskImportRepository: DiskImportRepository,
        private val disksRepository: DisksRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DiskImportViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DiskImportViewModel(
                    diskRequisitionsRepository,
                    diskTitlesRepository,
                    diskImportRepository,
                    disksRepository
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}