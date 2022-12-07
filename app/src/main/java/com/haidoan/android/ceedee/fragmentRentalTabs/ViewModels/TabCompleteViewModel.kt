package com.haidoan.android.ceedee.fragmentRentalTabs.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.haidoan.android.ceedee.data.Rental
import com.haidoan.android.ceedee.fragmentRentalTabs.Repository.RentalRepository

class TabCompleteViewModel : ViewModel() {

    private val repository: RentalRepository
    private val _completeRentals = MutableLiveData<ArrayList<Rental>>()

    val completeRentals: LiveData<ArrayList<Rental>> = _completeRentals

    init {
        repository = RentalRepository().getInstance()
        repository.loadUsers(_completeRentals, "Complete")
    }

}