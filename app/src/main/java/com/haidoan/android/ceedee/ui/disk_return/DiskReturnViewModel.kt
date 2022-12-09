package com.haidoan.android.ceedee.ui.disk_return

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.google.firebase.Timestamp
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.DisksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

private const val TAG = "DiskReturnViewModel"
private const val SAVED_STATE_KEY_RENTAL_ID = "currentRentalId"

class DiskReturnViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val diskRentalRepository: DiskRentalRepository,
    private val diskTitlesRepository: DiskTitlesRepository,
    private val disksRepository: DisksRepository
) : ViewModel() {

    val uiState: LiveData<DiskReturnUiState> =
        savedStateHandle.getLiveData<String>(SAVED_STATE_KEY_RENTAL_ID).switchMap { rentalId ->
            Log.d(TAG, "SavedState - rentalId: $rentalId")
            liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
                val rental = diskRentalRepository.getRentalStreamById(rentalId).first()
                diskTitlesRepository.getDiskTitlesByListOfId(rental.diskTitlesRentedAndAmount.keys.toList())
                    .collect { diskTitles: List<DiskTitle> ->
                        emit(
                            DiskReturnUiState(
                                customerName = rental.customerName,
                                customerPhone = rental.customerPhone,
                                customerAddress = rental.customerAddress,
                                diskTitlesToReturn = rental.diskTitlesRentedAndAmount.map { diskTitleIdAndAmount ->
                                    Triple(
                                        diskTitles.first { it.id == diskTitleIdAndAmount.key },
                                        diskTitleIdAndAmount.value,
                                        diskTitleIdAndAmount.value * 3000
                                    )
                                }.toList(),
                                dueDate = rental.dueDate?.toLocalDate(),
                                rentDate = rental.rentDate?.toLocalDate(),
                                returnDate = rental.returnDate?.toLocalDate()
                            )
                        )
                    }
            }
        }

    fun setRentalId(rentalId: String?) {
        savedStateHandle[SAVED_STATE_KEY_RENTAL_ID] = rentalId
        Log.d(TAG, "Called setRentalId - new id: $rentalId")
    }

    fun completeRental() {
        viewModelScope.launch {
            diskRentalRepository.completeRental(
                savedStateHandle.get<String>(SAVED_STATE_KEY_RENTAL_ID) ?: "",
                uiState.value?.totalPayment ?: 0L
            ).collect()
        }
        viewModelScope.launch {
            disksRepository.returnDisksRented(
                savedStateHandle.get<String>(SAVED_STATE_KEY_RENTAL_ID) ?: ""
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val diskRentalRepository: DiskRentalRepository,
        private val diskTitlesRepository: DiskTitlesRepository,
        private val disksRepository: DisksRepository,
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle? = null
    ) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

        override fun <T : ViewModel> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            return DiskReturnViewModel(
                handle,
                diskRentalRepository,
                diskTitlesRepository,
                disksRepository
            ) as T

        }
    }
}

data class DiskReturnUiState(
    val customerName: String? = "",
    val customerPhone: String? = "",
    val customerAddress: String? = "",
    /**
     * Pair<DiskTitle, Long> = diskTitle & its amount. Final "Long" = total fee for that disk title
     */
    val diskTitlesToReturn: List<Triple<DiskTitle, Long, Long>> = listOf(),
    val dueDate: LocalDate? = LocalDate.now(),
    val rentDate: LocalDate? = LocalDate.now(),
    val returnDate: LocalDate? = LocalDate.now(),

    ) {
    val overdueDateCount: Long =
        if (LocalDate.now().isBefore(dueDate)) 0
        else ChronoUnit.DAYS.between(
            dueDate,
            LocalDate.now()
        )

    //TODO: Fetch the overdueFeePerDay from database
    val overdueFee: Long = overdueDateCount * 4000
    val totalPayment = diskTitlesToReturn.sumOf { it.third }
}

fun Timestamp.toLocalDate(): LocalDate {
    val zoneId = ZoneId.of("Asia/Ho_Chi_Minh")
    return this.toDate().toInstant().atZone(zoneId).toLocalDate()
}



