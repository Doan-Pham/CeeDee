package com.haidoan.android.ceedee.ui.disk_return

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.google.firebase.Timestamp
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

private const val TAG = "DiskReturnViewModel"
private const val SAVED_STATE_KEY_RENTAL_ID = "currentRentalId"

class DiskReturnViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val diskRentalRepository: DiskRentalRepository,
    private val diskTitlesRepository: DiskTitlesRepository
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
                                disksToReturn = rental.diskTitlesRentedAndAmount.mapKeys { diskTitleAndAmount -> diskTitles.first { it.id == diskTitleAndAmount.key } },
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

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val diskRentalRepository: DiskRentalRepository,
        private val diskTitlesRepository: DiskTitlesRepository,
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle? = null
    ) :
        AbstractSavedStateViewModelFactory(owner, defaultArgs) {

        override fun <T : ViewModel> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            return DiskReturnViewModel(handle, diskRentalRepository, diskTitlesRepository) as T
        }
    }
}

data class DiskReturnUiState(
    val customerName: String? = "",
    val customerPhone: String? = "",
    val customerAddress: String? = "",
    val disksToReturn: Map<DiskTitle, Long>? = mapOf(),
    val dueDate: LocalDate? = LocalDate.now(),
    val rentDate: LocalDate? = LocalDate.now(),
    val returnDate: LocalDate? = LocalDate.now(),
    val totalPayment: Float? = 0f,
)

fun Timestamp.toLocalDate(): LocalDate {
    val zoneId = ZoneId.of("Asia/Ho_Chi_Minh")
    return this.toDate().toInstant().atZone(zoneId).toLocalDate()
}



