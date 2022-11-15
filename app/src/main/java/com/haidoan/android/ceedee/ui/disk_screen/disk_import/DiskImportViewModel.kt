package com.haidoan.android.ceedee.ui.disk_screen.disk_import

import androidx.lifecycle.*
import com.haidoan.android.ceedee.data.Requisition
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsRepository
import kotlinx.coroutines.Dispatchers

// TODO: Implement the Repository
class DiskImportViewModel(private val diskRequisitionsRepository: DiskRequisitionsRepository) :
    ViewModel() {
    private val _requisitionId = MutableLiveData("")

    private val _currentRequisition = _requisitionId.switchMap { requisitionId ->
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            diskRequisitionsRepository.getRequisitionStreamById(requisitionId).collect { emit(it) }
        }
    }

    val currentRequisition: LiveData<Requisition>
        get() = _currentRequisition

    fun setCurrentRequisitionId(requisitionId: String) {
        _requisitionId.value = requisitionId
    }

    class Factory(
        private val diskRequisitionsRepository: DiskRequisitionsRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DiskImportViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DiskImportViewModel(diskRequisitionsRepository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}