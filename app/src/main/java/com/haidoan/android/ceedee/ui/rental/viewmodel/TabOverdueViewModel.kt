package com.haidoan.android.ceedee.ui.rental.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.haidoan.android.ceedee.data.Rental
import com.haidoan.android.ceedee.ui.rental.repository.RentalRepository

class TabOverdueViewModel : ViewModel() {
    private val repository: RentalRepository = RentalRepository().getInstance()
    private val _completeRentals = MutableLiveData<ArrayList<Rental>>()

    val completeRentals: LiveData<ArrayList<Rental>> = _completeRentals

    init {
        repository.loadUsers(_completeRentals, "Overdue")
    }
}