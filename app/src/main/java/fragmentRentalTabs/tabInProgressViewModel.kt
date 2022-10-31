package fragmentRentalTabs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class tabInProgressViewModel :ViewModel(){
    private val repository : RentalRepository
    private val _completeRentals = MutableLiveData<ArrayList<Rental>>()

    val completeRentals : LiveData<ArrayList<Rental>> = _completeRentals

    init {
        repository = RentalRepository().getInstance()
        repository.loadUsers(_completeRentals,"In progress")
    }
}