package com.haidoan.android.ceedee.data.disk_rental

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.type.Date
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.Rental
import com.haidoan.android.ceedee.data.Requisition
import com.haidoan.android.ceedee.data.supplier.Supplier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId

class DiskRentalFiresoreDataSource {
    private val firestoreDb: FirebaseFirestore = FirebaseFirestore.getInstance()
    fun getRentalStream(): Flow<List<Rental>> =
        firestoreDb.collection("Rental").snapshots().map { querySnapshot ->
            querySnapshot.documents.map {
                Rental(
                    it.id,
                    it.get("customerName") as String,
                    it.get("customerAddress") as String,
                    it.get("customerPhone") as String,
                    it.get("diskTitlesToAdd") as Map<String, Long>,
                    it.get("dueDate") as Timestamp,
                    it.get("rentDate") as Timestamp,
                    it.get("totalPayment") as Timestamp,
                    // Naming a Firestore field as "status" somehow causes the app to crash
                    it.get("rentalStatus") as String
                )
            }
        }

    fun getRentalStreamById(rentalId: String): Flow<Rental> = flow {
        val result = firestoreDb.collection("Rental").document(rentalId).get().await()
        emit(
            Rental(
                result.id,
                result.get("customerName") as String,
                result.get("customerAddress") as String,
                result.get("customerPhone") as String,
                result.get("diskTitlesToImport") as Map<String, Long>,
                result.get("dueDate") as Timestamp,
                result.get("rentDate") as Timestamp,
                result.get("returnDate") as Timestamp,
                // Naming a Firestore field as "status" somehow causes the app to crash
                result.get("rentalStatus") as String
            )
        )
    }

    suspend fun completeRental(rentalId: String) =
        firestoreDb.collection("Rental").document(rentalId)
            .update("rentalStatus", "Completed").await()

    suspend fun addRental(
        customerName: String?,
        customerAddress: String?,
        customerPhone: String?,
        diskTitlesToAdd: Map<DiskTitle, Long>
    ): DocumentReference? {

        //val diskTitleIdToAmountMap = diskTitlesToAdd
        val newRentalAsMap = hashMapOf(
            "customerName" to (customerName?: "") ,
            "customerAddress" to (customerAddress ?: ""),
            "customerPhone" to (customerPhone ?: ""),
            "rentDate" to Timestamp.now(),
            "dueDate" to Timestamp(java.util.Date(LocalDate.now().plusDays(30).toString())),
            "returnDate" to Timestamp.now(),
            "rentalStatus" to "In progress",
            "diskTitlesToAdd" to diskTitlesToAdd.mapKeys { it.key.id },
            "totalPayment" to 0,
        )
        Log.d("RentalFirestore", "Called Addrental")
        return firestoreDb.collection("Rental").add(newRentalAsMap).await()
    }
}