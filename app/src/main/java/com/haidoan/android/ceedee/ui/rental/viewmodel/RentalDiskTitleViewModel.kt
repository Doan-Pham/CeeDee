package com.haidoan.android.ceedee.ui.rental.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.haidoan.android.ceedee.data.Rental
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import kotlinx.coroutines.Dispatchers

private const val TAG = "RentalDiskTitleVModel"
class RentalDiskTitleViewModel(
    private val diskTitlesRepository: DiskTitlesRepository,
) : ViewModel() {

    private var _currentRental = Rental()

    fun setCurrentRental(rental: Rental) {
        _currentRental = rental
        Log.d(TAG, "setCurrentRental() - _currentRental:$_currentRental ")
    }

    val currentRentalDiskTitles =
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            diskTitlesRepository.getDiskTitlesByListOfId(_currentRental.diskTitlesToAdd.keys.toList())
                .collect {
                    emit(it.map { diskTitle ->
                        diskTitle.copy(
                            diskAmount = _currentRental.diskTitlesToAdd[diskTitle.id]
                                ?: diskTitle.diskAmount
                        )
                    })
                }
        }

    class Factory(
        private val diskTitlesRepository: DiskTitlesRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RentalDiskTitleViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RentalDiskTitleViewModel(diskTitlesRepository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}