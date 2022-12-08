package com.haidoan.android.ceedee.ui.disk_return

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalRepository
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskTitlesRepository

class DiskReturnViewModel(
    savedStateHandle: SavedStateHandle,
    private val diskRentalRepository: DiskRentalRepository,
    private val diskTitlesRepository: DiskTitlesRepository
) : ViewModel() {

}

