package com.haidoan.android.ceedee.ui.customer_related

import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseUser
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.customer.Customer
import com.haidoan.android.ceedee.data.customer.CustomerRepository
import com.haidoan.android.ceedee.data.disk_rental.DiskRentalRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import com.haidoan.android.ceedee.ui.login.AuthenticationRepository
import com.haidoan.android.ceedee.ui.utils.toPhoneNumberWithoutCountryCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.toImmutableMap

private const val TAG = "CustomerActivityVM"

class CustomerActivityViewModel(
    private val authenticationRepository: AuthenticationRepository,
    private val customerRepository: CustomerRepository,
    private val diskRentalRepository: DiskRentalRepository
) :
    ViewModel() {

    var currentUser = MutableLiveData<FirebaseUser>()
    val isUserSignedIn = authenticationRepository.isUserSignedIn()
    val currentCustomer = MutableLiveData<Customer>(null)

    fun addOrUpdateCustomerInfo(customerPhone: String, customerName: String, customerAddress: String) =
        liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
            customerRepository.addOrUpdateCustomer(
                Customer(
                    id = currentUser.value?.uid ?: "UNKNOWN_UID",
                    phone = customerPhone,
                    fullName = customerName,
                    address = customerAddress
                )
            ).collect { emit(it) }
        }

    private val _disksToRentAndAmount =
        MutableLiveData<MutableMap<DiskTitle, Long>>(mutableMapOf())

    val disksToRentAndAmount: LiveData<MutableMap<DiskTitle, Long>>
        get() = _disksToRentAndAmount

    fun addDiskTitleToRent(diskTitle: DiskTitle) {
        // Have to write all of this, or else livedata won't update
        val currentDiskTitlesMap = _disksToRentAndAmount.value
        currentDiskTitlesMap?.put(diskTitle, 1)
        _disksToRentAndAmount.value = currentDiskTitlesMap!!
        //Log.d(TAG, "addDiskTitleToRent : ${disksToRent.value}")
    }

    fun incrementDiskTitleAmount(diskTitle: DiskTitle) {
        val currentDiskTitlesMap = _disksToRentAndAmount.value
        val diskTitleCurrentAmount = currentDiskTitlesMap?.get(diskTitle) ?: 1
        if (diskTitleCurrentAmount < diskTitle.diskInStoreAmount) {
            currentDiskTitlesMap?.put(
                diskTitle,
                diskTitleCurrentAmount + 1
            )
        }
        _disksToRentAndAmount.value = currentDiskTitlesMap!!
        //Log.d(TAG, "addDiskTitleToRent : ${disksToRent.value}")
    }

    fun decrementDiskTitleAmount(diskTitle: DiskTitle) {
        val currentDiskTitlesMap = _disksToRentAndAmount.value
        val diskTitleCurrentAmount = currentDiskTitlesMap?.get(diskTitle) ?: 1
        if (diskTitleCurrentAmount > 1) {
            currentDiskTitlesMap?.put(
                diskTitle,
                diskTitleCurrentAmount - 1
            )
        }
        _disksToRentAndAmount.value = currentDiskTitlesMap!!
        //Log.d(TAG, "addDiskTitleToRent : ${disksToRent.value}")
    }

    fun removeDiskTitleToRent(diskTitle: DiskTitle) {
        val currentDiskTitlesMap = _disksToRentAndAmount.value
        currentDiskTitlesMap?.remove(diskTitle)
        _disksToRentAndAmount.value = currentDiskTitlesMap!!
        //Log.d(TAG, "addDiskTitleToRent : ${disksToRent.value}")
    }

    fun clearDiskTitleToRent() {
        _disksToRentAndAmount.value = mutableMapOf()
    }

    fun resetUser() {
        currentUser.value =
            authenticationRepository.currentUser
        Log.d(
            TAG,
            "resetUser() - currentUser: ${currentUser.value?.phoneNumber?.toPhoneNumberWithoutCountryCode()}"
        )
        updateCurrentCustomer()
    }

    fun updateCurrentCustomer() {
        viewModelScope.launch {
            currentCustomer.postValue(
                customerRepository.getCustomerByPhone(
                    currentUser.value?.phoneNumber?.toPhoneNumberWithoutCountryCode() ?: ""
                ).firstOrNull()
            )
            Log.d(TAG, "updateCurrentCustomer() - currentCustomer: ${currentCustomer.value?.fullName}")
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authenticationRepository.signOut()
        }
    }

    fun requestRental(customerPhone: String, customerName: String, customerAddress: String) =     liveData(context = viewModelScope.coroutineContext + Dispatchers.IO) {
        emit(Response.Loading())

        diskRentalRepository.addRental(
            customerName,
            customerAddress,
            customerPhone,
            _disksToRentAndAmount.value?.toImmutableMap()!!,
            "In request"
        ).collect {
                response ->
//            if (response is Response.Success) {
//                disksRepository.rentDisksOfDiskTitleIds(
//                    response.data?.id ?: "",
//                    disksToRent.value?.mapKeys { it.key.id } ?: mutableMapOf()).collect {
//                    diskTitlesRepository.updateDiskInStoreAmount(
//                        disksToRent.value?.keys?.map { it.id } ?: listOf()).collect()
//                }
//            }
            emit(response)
        }
    }


    class Factory(
        private val authenticationRepository: AuthenticationRepository,
        private val customerRepository: CustomerRepository,
        private val diskRentalRepository: DiskRentalRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CustomerActivityViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CustomerActivityViewModel(authenticationRepository, customerRepository, diskRentalRepository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}