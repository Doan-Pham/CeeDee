package com.haidoan.android.ceedee.fragmentRentalTabs.ViewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.haidoan.android.ceedee.fragmentRentalTabs.Rental
import com.haidoan.android.ceedee.fragmentRentalTabs.RentalRepository

class TabOverdueViewModel :ViewModel() {
    private val repository : RentalRepository = RentalRepository().getInstance()
    private val _completeRentals = MutableLiveData<ArrayList<Rental>>()

    val completeRentals : LiveData<ArrayList<Rental>> = _completeRentals
    init {
        repository.loadUsers(_completeRentals,"Overdue")
    }
}