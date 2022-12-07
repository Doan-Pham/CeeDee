package com.haidoan.android.ceedee.ui.rental.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.haidoan.android.ceedee.data.Rental
import com.haidoan.android.ceedee.ui.rental.repository.RentalRepository

class RentalViewModel : ViewModel() {

    private val repository: RentalRepository = RentalRepository().getInstance()
    private val _allRentals = MutableLiveData<ArrayList<Rental>>()

    val allRentals: LiveData<ArrayList<Rental>> = _allRentals

    init {
        repository.loadUsers(_allRentals, "All")
    }

}