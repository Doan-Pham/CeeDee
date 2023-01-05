package com.haidoan.android.ceedee.ui.customer_related.disk

import androidx.lifecycle.*
import com.haidoan.android.ceedee.data.Rental
import com.haidoan.android.ceedee.data.RentalStatus
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalRepository
import com.haidoan.android.ceedee.data.disk_rental.RentalStatusRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import kotlinx.coroutines.Dispatchers

class CustomerDiskViewModel(
    private val diskRentalRepository: DiskRentalRepository,
    private val rentalStatusRepository: RentalStatusRepository
) : ViewModel() {

    private val _diskTitles = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
        diskRentalRepository.getRentalsStream().collect { emit(it) }
    }
    val diskTitles: LiveData<List<Rental>>
        get() = _diskTitles


    private val _rentalStatus = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
        rentalStatusRepository.getRentalStatusStream().collect {
            if (it is Response.Success) {
                it.data.collect { emit(it) }
            }
        }
    }
    val rentalStatus: LiveData<List<RentalStatus>>
        get() = _rentalStatus

    class Factory(
        private val diskRentalRepository: DiskRentalRepository,
        private val rentalStatusRepository: RentalStatusRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CustomerDiskViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CustomerDiskViewModel(diskRentalRepository, rentalStatusRepository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}