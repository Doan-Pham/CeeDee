package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import androidx.lifecycle.*
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsRepository
import com.haidoan.android.ceedee.data.supplier.Supplier
import com.haidoan.android.ceedee.data.supplier.SupplierRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import kotlinx.coroutines.Dispatchers

class NewRequisitionViewModel(
    private val diskRequisitionsRepository: DiskRequisitionsRepository,
    private val diskTitlesRepository: DiskTitlesRepository,
    private val supplierRepository: SupplierRepository,
) : ViewModel() {

    val allSuppliers: LiveData<List<Supplier>>
        get() = _allSuppliers

    private val _allSuppliers =
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            supplierRepository.getSuppliersStream().collect {
                emit(it)
            }
        }

    val disksToImport: LiveData<Map<DiskTitle, Long>>
        get() = _disksToImport
    private val _disksToImport = MutableLiveData<Map<DiskTitle, Long>>(
        mapOf(
            DiskTitle(name = "Ha") to 1.toLong(), DiskTitle(name = "Ba") to 1.toLong()
        )

    )

    class Factory(
        private val diskRequisitionsRepository: DiskRequisitionsRepository,
        private val diskTitlesRepository: DiskTitlesRepository,
        private val supplierRepository: SupplierRepository,
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NewRequisitionViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NewRequisitionViewModel(
                    diskRequisitionsRepository,
                    diskTitlesRepository,
                    supplierRepository,
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}