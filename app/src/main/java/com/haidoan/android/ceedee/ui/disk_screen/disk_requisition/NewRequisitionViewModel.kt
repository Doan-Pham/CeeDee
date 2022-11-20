package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import androidx.lifecycle.*
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsRepository
import com.haidoan.android.ceedee.data.supplier.Supplier
import com.haidoan.android.ceedee.data.supplier.SupplierRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import kotlinx.coroutines.Dispatchers

private const val TAG = "NewRequisitionViewModel"

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

    val disksToImport: LiveData<MutableMap<DiskTitle, Long>>
        get() = _diskTitlesToImport

    private val _diskTitlesToImport = MutableLiveData<MutableMap<DiskTitle, Long>>(mutableMapOf())

    fun addDiskTitleToImport(diskTitle: DiskTitle) {
        // Have to write all of this, or else livedata won't update
        val currentDiskTitlesMap = _diskTitlesToImport.value
        currentDiskTitlesMap?.put(diskTitle, 1)
        _diskTitlesToImport.value = currentDiskTitlesMap!!
        //Log.d(TAG, "addDiskTitleToImport : ${disksToImport.value}")
    }

    fun incrementDiskTitleAmount(diskTitle: DiskTitle) {
        val currentDiskTitlesMap = _diskTitlesToImport.value
        val diskTitleCurrentAmount = currentDiskTitlesMap?.get(diskTitle) ?: 1
        if (diskTitleCurrentAmount < diskTitle.diskAmount) {
            currentDiskTitlesMap?.put(
                diskTitle,
                diskTitleCurrentAmount + 1
            )
        }

        _diskTitlesToImport.value = currentDiskTitlesMap!!
        //Log.d(TAG, "addDiskTitleToImport : ${disksToImport.value}")
    }

    fun decrementDiskTitleAmount(diskTitle: DiskTitle) {
        val currentDiskTitlesMap = _diskTitlesToImport.value
        val diskTitleCurrentAmount = currentDiskTitlesMap?.get(diskTitle) ?: 1
        if (diskTitleCurrentAmount > 1) {
            currentDiskTitlesMap?.put(
                diskTitle,
                diskTitleCurrentAmount - 1
            )
        }
        _diskTitlesToImport.value = currentDiskTitlesMap!!
        //Log.d(TAG, "addDiskTitleToImport : ${disksToImport.value}")
    }


    private val _supplierOfNewRequisition = MutableLiveData<Supplier>()

    fun setSupplierOfNewRequisition(supplier: Supplier) {
        _supplierOfNewRequisition.value = supplier
        //Log.d(TAG, "Current supplier: ${_supplierOfNewRequisition.value}")
    }


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