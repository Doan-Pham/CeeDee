package com.haidoan.android.ceedee.ui.customer_related.rental

import android.util.Log
import androidx.lifecycle.*
import com.haidoan.android.ceedee.data.Rental
import com.haidoan.android.ceedee.data.RentalStatus
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalRepository
import com.haidoan.android.ceedee.data.disk_rental.RentalStatusRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import com.haidoan.android.ceedee.ui.utils.toPhoneNumberWithoutCountryCode
import kotlinx.coroutines.Dispatchers

private const val TAG = "CustomerRentalViewModel"

class CustomerRentalViewModel(
    private val diskRentalRepository: DiskRentalRepository,
    private val rentalStatusRepository: RentalStatusRepository
) : ViewModel() {

    private val _currentCustomerPhone = MutableLiveData<String>()

    fun setCurrentCustomerPhone(customerPhone: String) {
        _currentCustomerPhone.value = customerPhone.toPhoneNumberWithoutCountryCode()
        Log.d(
            TAG,
            "setCurrentCustomerPhone() - _currentCustomerPhone: ${_currentCustomerPhone.value}"
        )
    }

    private val _currentFilteringStatus = MutableLiveData("")
    fun setFilteringStatus(status: String = "") {
        _currentFilteringStatus.value = status
        Log.d(
            TAG,
            "setFilteringStatus() - _currentFilteringStatus.value: ${_currentFilteringStatus.value}"
        )
    }

    private val rentalsModifications =
        MediatorLiveData<Pair<String?, String?>>().apply {
            addSource(_currentCustomerPhone) { value = Pair(it, _currentFilteringStatus.value) }
            addSource(_currentFilteringStatus) { value = Pair(_currentCustomerPhone.value, it) }
        }

    val rentals = rentalsModifications.switchMap { rentalsModifications ->
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            diskRentalRepository.getRentalsByCustomerPhoneStream(rentalsModifications.first ?: "")
                .collect {
                    emit(
                        it.filterByStatus(
                            rentalsModifications.second ?: ""
                        )
                    )
                }
        }
    }


    private val _rentalStatus = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
        rentalStatusRepository.getRentalStatusStream().collect {
            if (it is Response.Success) {
                it.data.collect { emit(it) }
            }
        }
    }
    val rentalStatus: LiveData<List<RentalStatus>>
        get() = _rentalStatus


    private fun List<Rental>.filterByStatus(status: String) =
        this.filter { individualRental ->
//            Log.d(TAG, "individualRental.rentalStatus: ${individualRental.rentalStatus}, status: $status")
            individualRental.rentalStatus == status
        }


    class Factory(
        private val diskRentalRepository: DiskRentalRepository,
        private val rentalStatusRepository: RentalStatusRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CustomerRentalViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CustomerRentalViewModel(diskRentalRepository, rentalStatusRepository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}