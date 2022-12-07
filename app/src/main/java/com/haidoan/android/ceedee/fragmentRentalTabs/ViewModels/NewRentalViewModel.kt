package com.haidoan.android.ceedee.fragmentRentalTabs.ViewModels

import android.util.Log
import androidx.lifecycle.*
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalRepository
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsRepository
import com.haidoan.android.ceedee.data.supplier.SupplierRepository
import com.haidoan.android.ceedee.ui.disk_screen.disk_requisition.NewRequisitionViewModel
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.Dispatchers
import okhttp3.internal.toImmutableMap

class NewRentalViewModel(private val diskRentalRepository: DiskRentalRepository) : ViewModel() {

    val disksToRent: LiveData<MutableMap<DiskTitle, Long>>
        get() = _diskTitlesToRent

    private val _diskTitlesToRent = MutableLiveData<MutableMap<DiskTitle, Long>>(mutableMapOf())
    fun addDiskTitleToRent(diskTitle: DiskTitle) {
        // Have to write all of this, or else livedata won't update
        val currentDiskTitlesMap = _diskTitlesToRent.value
        currentDiskTitlesMap?.put(diskTitle, 1)
        _diskTitlesToRent.value = currentDiskTitlesMap!!
        //Log.d(TAG, "addDiskTitleToImport : ${disksToImport.value}")
    }

    fun incrementDiskTitleAmount(diskTitle: DiskTitle) {
        val currentDiskTitlesMap = _diskTitlesToRent.value
        val diskTitleCurrentAmount = currentDiskTitlesMap?.get(diskTitle) ?: 1
        if (diskTitleCurrentAmount < diskTitle.diskAmount) {
            currentDiskTitlesMap?.put(
                diskTitle,
                diskTitleCurrentAmount + 1
            )
        }

        _diskTitlesToRent.value = currentDiskTitlesMap!!
        //Log.d(TAG, "addDiskTitleToImport : ${disksToImport.value}")
    }

    fun decrementDiskTitleAmount(diskTitle: DiskTitle) {
        val currentDiskTitlesMap = _diskTitlesToRent.value
        val diskTitleCurrentAmount = currentDiskTitlesMap?.get(diskTitle) ?: 1
        if (diskTitleCurrentAmount > 1) {
            currentDiskTitlesMap?.put(
                diskTitle,
                diskTitleCurrentAmount - 1
            )
        }
        _diskTitlesToRent.value = currentDiskTitlesMap!!
        //Log.d(TAG, "addDiskTitleToImport : ${disksToImport.value}")
    }

    fun removeDiskTitleToImport(diskTitle: DiskTitle) {
        val currentDiskTitlesMap = _diskTitlesToRent.value
        currentDiskTitlesMap?.remove(diskTitle)
        _diskTitlesToRent.value = currentDiskTitlesMap!!
        //Log.d(TAG, "addDiskTitleToImport : ${disksToImport.value}")
    }

    fun addRental() =
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(Response.Loading())
            diskRentalRepository.addRental(
                _customerName.value,
                _customerAddress.value,
                _customerPhone.value,
                _diskTitlesToRent.value?.toImmutableMap()!!
            ).collect { response -> emit(response) }
        }

    private val _customerName = MutableLiveData<String>()
    private val _customerPhone = MutableLiveData<String>()
    private val _customerAddress = MutableLiveData<String>()
    fun setCustomerInformation(
        customerName: String,
        customerAddress: String,
        customerPhone: String
    ) {
        _customerName.value = customerName
        _customerAddress.value = customerAddress
        _customerPhone.value = customerPhone
    }

    class Factory(
        private val diskRentalRepository: DiskRentalRepository,
        private val diskTitlesRepository: DiskTitlesRepository,
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NewRentalViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NewRentalViewModel(
                    diskRentalRepository,
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}