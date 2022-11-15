package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import androidx.lifecycle.*
import com.haidoan.android.ceedee.data.Requisition
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsRepository
import kotlinx.coroutines.Dispatchers

class DiskRequisitionsViewModel(
    private val diskRequisitionsRepository: DiskRequisitionsRepository
) : ViewModel() {

    private val searchQuery = MutableLiveData("")

    private val _requisitions = searchQuery.switchMap {
        liveData(Dispatchers.IO) {
            diskRequisitionsRepository.getRequisitionsStream()
                .collect { requisitions ->
                    emit(requisitions.filter { individualRequisition ->
                        individualRequisition.supplierName.lowercase()
                            .contains(
                                searchQuery.value.toString().lowercase()
                            )
                    })
                }
        }
    }
    val requisitions: LiveData<List<Requisition>> = _requisitions


    fun searchRequisition(query: String?) {
        searchQuery.value = query ?: ""
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