package com.haidoan.android.ceedee.data.customer

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.tasks.await

class CustomerFireStoreDataSource {
    private val firestoreDb: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun getCustomersStream(): Flow<List<Customer>> =
        firestoreDb.collection("Customer").snapshots().mapNotNull { querySnapshot ->
            querySnapshot.documents.mapNotNull {
                it.toObject(Customer::class.java)
            }
        }

    suspend fun addCustomer(customer: Customer): DocumentReference? {
        val newCustomer = hashMapOf(
            "address" to customer.address,
            "phone" to customer.phone,
            "fullName" to customer.fullName
        )
        return firestoreDb.collection("Customer").add(newCustomer).await()
    }

    suspend fun updateCustomer(
        id: String,
        address: String,
        phone: String,
        fullName: String
    ): Void? {
        return firestoreDb.collection("Customer").document(id).update(
            "address", address,
            "phone", phone,
            "fullName", fullName
        ).await()
    }

}