package fragmentRentalTabs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RentalViewModel : ViewModel() {

    private val repository : RentalRepository
    private val _allRentals = MutableLiveData<ArrayList<Rental>>()

    val allRentals : LiveData<ArrayList<Rental>> = _allRentals

    init {
        repository = RentalRepository().getInstance()
        repository.loadUsers(_allRentals,"All")
    }

}