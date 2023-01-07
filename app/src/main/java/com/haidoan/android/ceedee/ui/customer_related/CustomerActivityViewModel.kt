package com.haidoan.android.ceedee.ui.customer_related

import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseUser
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.customer.Customer
import com.haidoan.android.ceedee.data.customer.CustomerRepository
import com.haidoan.android.ceedee.ui.login.AuthenticationRepository
import com.haidoan.android.ceedee.ui.utils.toPhoneNumberWithoutCountryCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "CustomerActivityVM"

class CustomerActivityViewModel(
    private val authenticationRepository: AuthenticationRepository,
    private val customerRepository: CustomerRepository
) :
    ViewModel() {

    var currentUser = MutableLiveData<FirebaseUser>()
    val isUserSignedIn = authenticationRepository.isUserSignedIn()
    val currentCustomer = MutableLiveData<Customer>(null)

    fun addOrUpdate(customerPhone: String, customerName: String, customerAddress: String) =
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

    class Factory(
        private val authenticationRepository: AuthenticationRepository,
        private val customerRepository: CustomerRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CustomerActivityViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CustomerActivityViewModel(authenticationRepository, customerRepository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}