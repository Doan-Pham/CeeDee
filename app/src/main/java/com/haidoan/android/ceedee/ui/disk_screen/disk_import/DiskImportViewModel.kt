package com.haidoan.android.ceedee.ui.disk_screen.disk_import

import androidx.lifecycle.*
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.Requisition
import com.haidoan.android.ceedee.data.disk_import.DiskImportRepository
import com.haidoan.android.ceedee.data.disk_import.Import
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.DisksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

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
    ) {
        viewModelScope.launch {
            coroutineScope {
                launch { diskImportRepository.addImport(newImport) }
                launch { disksRepository.addMultipleDisks(diskTitlesToImportAndAmount) }
                launch { diskRequisitionsRepository.completeRequisition(requisitionId) }
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