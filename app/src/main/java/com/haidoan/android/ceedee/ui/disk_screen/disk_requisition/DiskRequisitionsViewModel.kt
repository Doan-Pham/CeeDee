package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import androidx.lifecycle.*
import com.haidoan.android.ceedee.data.Requisition
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsRepository
import kotlinx.coroutines.Dispatchers


class DiskRequisitionsViewModel(
    private val diskRequisitionsRepository: DiskRequisitionsRepository
) : ViewModel() {

    private val filteringCategory =
        MutableLiveData(DiskRequisitionFilterCategory.FILTER_BY_PENDING)

    private val searchQuery = MutableLiveData("")

    // This will observe all the modification events (searching, filtering) and combine into
    // one LiveData
    private val requisitionsModifications =
        MediatorLiveData<Pair<DiskRequisitionFilterCategory?, String?>>().apply {
            addSource(filteringCategory) { value = Pair(it, searchQuery.value) }
            addSource(searchQuery) { value = Pair(filteringCategory.value, it) }
        }

    private val _requisitions = requisitionsModifications.switchMap { requisitionsModifications ->
        liveData(Dispatchers.IO) {
            diskRequisitionsRepository.getRequisitionsStream()
                .collect { requisitions ->
                    val currentFilteringCategory = requisitionsModifications.first
                        ?: DiskRequisitionFilterCategory.FILTER_BY_PENDING
                    val currentSearchQuery = requisitionsModifications.second ?: ""
                    emit(
                        requisitions.sortedByDescending { it.sentDate }
                            .searchBySupplierName(currentSearchQuery)
                            .filter(currentFilteringCategory)
                    )
                }
        }
    }
    val requisitions: LiveData<List<Requisition>> = _requisitions

    fun searchRequisition(query: String?) {
        searchQuery.value = query ?: ""
    }

    fun setFilteringCategory(inputFilteringCategory: DiskRequisitionFilterCategory) {
        filteringCategory.value = inputFilteringCategory
    }

    private fun List<Requisition>.searchBySupplierName(supplierName: String) =
        this.filter { individualRequisition ->
            individualRequisition.supplierName.lowercase()
                .contains(
                    supplierName.lowercase()
                )
        }

    private fun List<Requisition>.filter(filteringCategory: DiskRequisitionFilterCategory) =
        this.filter { individualRequisition ->
            when (filteringCategory) {
                DiskRequisitionFilterCategory.FILTER_BY_PENDING -> individualRequisition.requisitionStatus == "Pending"
                DiskRequisitionFilterCategory.FILTER_BY_COMPLETED -> individualRequisition.requisitionStatus == "Completed"
                DiskRequisitionFilterCategory.FILTER_BY_ALL -> true
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

enum class DiskRequisitionFilterCategory {
    FILTER_BY_COMPLETED,
    FILTER_BY_PENDING,
    FILTER_BY_ALL
}