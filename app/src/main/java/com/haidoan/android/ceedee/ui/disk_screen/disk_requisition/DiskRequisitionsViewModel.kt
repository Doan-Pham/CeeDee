package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.haidoan.android.ceedee.data.Requisition
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsRepository
import kotlinx.coroutines.Dispatchers

class DiskRequisitionsViewModel(
    private val diskRequisitionsRepository: DiskRequisitionsRepository
) :
    ViewModel() {
    private val _requisitions = liveData(Dispatchers.IO) {
        diskRequisitionsRepository.getRequisitionsStream().collect { emit(it) }
    }

    val requisitions: LiveData<List<Requisition>> = _requisitions

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