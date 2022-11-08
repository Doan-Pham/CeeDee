package com.haidoan.android.ceedee.fragmentRentalTabs.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.haidoan.android.ceedee.fragmentRentalTabs.Rental
import com.haidoan.android.ceedee.fragmentRentalTabs.RentalRepository

class RentalViewModel : ViewModel() {

    private val repository : RentalRepository = RentalRepository().getInstance()
    private val _allRentals = MutableLiveData<ArrayList<Rental>>()

    val allRentals : LiveData<ArrayList<Rental>> = _allRentals

    init {
        repository.loadUsers(_allRentals,"All")
    }

}