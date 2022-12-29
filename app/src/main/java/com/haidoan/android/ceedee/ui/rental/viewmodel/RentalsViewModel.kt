package com.haidoan.android.ceedee.ui.rental.viewmodel

import androidx.lifecycle.*
import com.haidoan.android.ceedee.data.Rental
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "RentalsViewModel"
class RentalsViewModel(
    private val rentalsRepository: DiskRentalRepository
) : ViewModel() {

    private val filteringCategory =
        MutableLiveData(RentalFilterCategory.FILTER_BY_IN_PROGRESS)

    private val searchQuery = MutableLiveData("")

    // This will observe all the modification events (searching, filtering) and combine into
    // one LiveData
    private val rentalsModifications =
        MediatorLiveData<Pair<RentalFilterCategory?, String?>>().apply {
            addSource(filteringCategory) { value = Pair(it, searchQuery.value) }
            addSource(searchQuery) { value = Pair(filteringCategory.value, it) }
        }

    private val _rentals = rentalsModifications.switchMap { rentalsModifications ->
        liveData(Dispatchers.IO) {
            rentalsRepository.getRentalsStream()
                .collect { rentals ->
                    val currentFilteringCategory = rentalsModifications.first
                        ?: RentalFilterCategory.FILTER_BY_IN_PROGRESS
                    val currentSearchQuery = rentalsModifications.second ?: ""
                    emit(
                        rentals.searchByCustomerName(currentSearchQuery)
                            .filter(currentFilteringCategory)
                    )
                }
        }
    }
    val rentals: LiveData<List<Rental>> = _rentals

    fun searchRental(query: String?) {
        searchQuery.value = query ?: ""
    }

    fun setFilteringCategory(inputFilteringCategory: RentalFilterCategory) {
        filteringCategory.value = inputFilteringCategory
    }

    fun acceptRentalInRequest(rentalId: String) {
        viewModelScope.launch {
            rentalsRepository.acceptRentalInRequest(rentalId)
        }
    }


    private fun List<Rental>.searchByCustomerName(supplierName: String) =
        this.filter { individualRental ->
            individualRental.customerName?.lowercase()
                ?.contains(
                    supplierName.lowercase()
                ) ?: false
        }

    private fun List<Rental>.filter(filteringCategory: RentalFilterCategory) =
        this.filter { individualRental ->
            when (filteringCategory) {
                RentalFilterCategory.FILTER_BY_IN_PROGRESS -> individualRental.rentalStatus == "In progress"
                RentalFilterCategory.FILTER_BY_COMPLETE -> individualRental.rentalStatus == "Complete"
                RentalFilterCategory.FILTER_BY_OVERDUE -> individualRental.rentalStatus == "Overdue"
                RentalFilterCategory.FILTER_BY_IN_REQUEST -> individualRental.rentalStatus == "In request"
                RentalFilterCategory.FILTER_BY_ALL -> true
            }
        }

    class Factory(
        private val rentalsRepository: DiskRentalRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RentalsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RentalsViewModel(rentalsRepository) as T
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
    FILTER_BY_ALL
}