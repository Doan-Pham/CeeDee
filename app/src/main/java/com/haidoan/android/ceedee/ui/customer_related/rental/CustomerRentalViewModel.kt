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

    private val filteringCategory =
        MutableLiveData(RentalFilterCategory.FILTER_BY_IN_PROGRESS)

    private val rentalsModifications =
        MediatorLiveData<Pair<String?, RentalFilterCategory?>>().apply {
            addSource(_currentCustomerPhone) { value = Pair(it, filteringCategory.value) }
            addSource(filteringCategory) { value = Pair(_currentCustomerPhone.value, it) }
        }

    val rentals = rentalsModifications.switchMap { rentalsModifications ->
        liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            diskRentalRepository.getRentalsByCustomerPhoneStream(rentalsModifications.first ?: "")
                .collect {
                    emit(
                        it.filter(
                            rentalsModifications.second ?: RentalFilterCategory.FILTER_BY_IN_REQUEST
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

    fun setFilteringCategory(inputFilteringCategory: RentalFilterCategory) {
        filteringCategory.value = inputFilteringCategory
    }

    private fun List<Rental>.filter(filteringCategory: RentalFilterCategory) =
        this.filter { individualRental ->
            when (filteringCategory) {
                RentalFilterCategory.FILTER_BY_IN_PROGRESS -> individualRental.rentalStatus == "In progress"
                RentalFilterCategory.FILTER_BY_COMPLETE -> individualRental.rentalStatus == "Complete"
                RentalFilterCategory.FILTER_BY_OVERDUE -> individualRental.rentalStatus == "Overdue"
                RentalFilterCategory.FILTER_BY_IN_REQUEST -> individualRental.rentalStatus == "In request"
            }
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

enum class RentalFilterCategory {
    FILTER_BY_COMPLETE,
    FILTER_BY_OVERDUE,
    FILTER_BY_IN_PROGRESS,
    FILTER_BY_IN_REQUEST,
}