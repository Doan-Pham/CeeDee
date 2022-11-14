package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import androidx.lifecycle.*
import com.haidoan.android.ceedee.data.Requisition
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsRepository
import kotlinx.coroutines.launch

class DiskRequisitionsViewModel(
    private val diskRequisitionsRepository: DiskRequisitionsRepository
) :
    ViewModel() {
    private val _requisitions = MutableLiveData<List<Requisition>>()
    val requisitions: LiveData<List<Requisition>> = _requisitions

    init {
        refreshRequisitions()
    }

    private fun refreshRequisitions() {
        viewModelScope.launch {
            diskRequisitionsRepository.getAllRequisitions().collect {
                _requisitions.value = it
            }
        }
    }

    class Factory(
        private val diskRequisitionsRepository: DiskRequisitionsRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DiskRequisitionsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DiskRequisitionsViewModel(diskRequisitionsRepository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}