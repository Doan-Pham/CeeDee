package com.haidoan.android.ceedee.ui.rental.viewmodel

import androidx.lifecycle.*
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.customer.Customer
import com.haidoan.android.ceedee.data.customer.CustomerRepository
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.DisksRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.Dispatchers
import okhttp3.internal.toImmutableMap

class NewRentalViewModel(
    private val diskRentalRepository: DiskRentalRepository,
    private val disksRepository: DisksRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {
    private var isCurrentUserCustomer = false

    fun setIsCurrentUserCustomer(isCustomer: Boolean) {
        isCurrentUserCustomer = isCustomer
    }

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

    fun clearDiskTitleToRent() {
        _diskTitlesToRent.value = mutableMapOf()
    }

    fun incrementDiskTitleAmount(diskTitle: DiskTitle) {
        val currentDiskTitlesMap = _diskTitlesToRent.value
        val diskTitleCurrentAmount = currentDiskTitlesMap?.get(diskTitle) ?: 1
        if (diskTitleCurrentAmount < diskTitle.diskInStoreAmount) {
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
            val rentalStatus =
                if (isCurrentUserCustomer) "In request"
                else "In progress"

            diskRentalRepository.addRental(
                _customerName.value,
                _customerAddress.value,
                _customerPhone.value,
                _diskTitlesToRent.value?.toImmutableMap()!!,
                rentalStatus
            ).collect { response ->
                if (response is Response.Success) {
                    disksRepository.rentDisksOfDiskTitleIds(
                        response.data?.id ?: "",
                        disksToRent.value?.mapKeys { it.key.id } ?: mutableMapOf())
                }
                emit(response)
            }
        }

    fun proceedCustomer() = liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
        if (isExistsCustomer()) {
            customerRepository.updateCustomer(
                _customerId.value!!,
                _customerAddress.value,
                _customerPhone.value,
                _customerName.value
            ).collect { emit(it) }
        } else {
            customerRepository.addCustomer(
                _customerName.value,
                _customerAddress.value,
                _customerPhone.value
            ).collect { emit(it) }
        }
    }

    private fun isExistsCustomer(): Boolean {
        _allCustomers.value?.forEach {
            if (_customerPhone.value.equals(it.phone)) {
                return true
            }
        }
        return false
    }

    val allCustomers: LiveData<List<Customer>>
        get() = _allCustomers

    private val _allCustomers =
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            customerRepository.getCustomersStream().collect {
                emit(it)
            }
        }
    private val _customerName = MutableLiveData<String>()
    private val _customerPhone = MutableLiveData<String>()
    private val _customerAddress = MutableLiveData<String>()
    private val _customerId = MutableLiveData<String>()
    fun setCustomerInformation(
        idCustomer: String,
        customerName: String,
        customerAddress: String,
        customerPhone: String,
    ) {
        _customerId.value = idCustomer
        _customerName.value = customerName
        _customerAddress.value = customerAddress
        _customerPhone.value = customerPhone
    }

    class Factory(
        private val diskRentalRepository: DiskRentalRepository,
        private val disksRepository: DisksRepository,
        private val customerRepository: CustomerRepository,
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NewRentalViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return NewRentalViewModel(
                    diskRentalRepository, disksRepository, customerRepository
                ) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}