package com.haidoan.android.ceedee.data.user_management

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.snapshots
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.Rental
import com.haidoan.android.ceedee.data.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.ZoneId

@Suppress("UNCHECKED_CAST")


class UserFirestoreDataSource {
    private val firestoreDb: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val zoneId = ZoneId.of("Asia/Ho_Chi_Minh")

    fun getUsersStream(): Flow<List<User>> =
        firestoreDb.collection("User").snapshots()
            .mapNotNull { it.toObjects(User::class.java).toList() }

    fun getRentalStreamById(rentalId: String): Flow<Rental> = flow {
        val result = firestoreDb.collection("Rental").document(rentalId).get().await()
        emit(
            Rental(
                result.id,
                result.get("customerName") as String,
                result.get("customerAddress") as String,
                result.get("customerPhone") as String,
                result.get("diskTitlesToAdd") as Map<String, Long>,
                result.get("dueDate") as Timestamp,
                result.get("rentDate") as Timestamp,
                result.get("returnDate") as Timestamp,
                // Naming a Firestore field as "status" somehow causes the app to crash
                result.get("rentalStatus") as String
            )
        )
    }

    suspend fun completeRental(rentalId: String, totalPayment: Long) =
        firestoreDb.collection("Rental").document(rentalId)
            .update(
                mapOf(
                    "rentalStatus" to "Complete",
                    "totalPayment" to totalPayment,
                    "returnDate" to Timestamp.now()
                )
            ).await()

    suspend fun addRental(
        customerName: String?,
        customerAddress: String?,
        customerPhone: String?,
        diskTitlesToAdd: Map<DiskTitle, Long>
    ): DocumentReference? {

        //val diskTitleIdToAmountMap = diskTitlesToAdd
        val newRentalAsMap = hashMapOf(
            "customerName" to (customerName ?: ""),
            "customerAddress" to (customerAddress ?: ""),
            "customerPhone" to (customerPhone ?: ""),
            "rentDate" to Timestamp.now(),
            "dueDate" to Timestamp(
                (LocalDate.now().plusDays(30).atStartOfDay(zoneId).toEpochSecond()), 0
            ),
            "returnDate" to Timestamp.now(),
            "rentalStatus" to "In progress",
            "diskTitlesToAdd" to diskTitlesToAdd.mapKeys { it.key.id },
            "totalPayment" to 0,
        )
        Log.d("RentalFirestore", "Called Addrental")
        return firestoreDb.collection("Rental").add(newRentalAsMap).await()
    }
}