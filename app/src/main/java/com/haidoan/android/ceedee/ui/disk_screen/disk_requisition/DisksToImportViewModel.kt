package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import androidx.lifecycle.*
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import kotlinx.coroutines.Dispatchers

class DisksToImportViewModel(private val diskTitlesRepository: DiskTitlesRepository) :
    ViewModel() {
    private val _diskTitlesInStore =
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            diskTitlesRepository.getDiskTitlesAvailableInStore()
                .collect { emit(it) }
        }

    val diskTitlesInStore: LiveData<List<DiskTitle>>
        get() = _diskTitlesInStore

    class Factory(
        private val diskTitlesRepository: DiskTitlesRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DisksToImportViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DisksToImportViewModel(
                    diskTitlesRepository
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}