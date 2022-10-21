package com.haidoan.android.ceedee.ui.report

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters.firstDayOfMonth
import java.time.temporal.TemporalAdjusters.lastDayOfMonth

private const val TAG = "FirestoreApi.kt"

class FirestoreApi {
    private val db = Firebase.firestore

    fun getRevenueBetweenMonths(
        startTime: LocalDate,
        endTime: LocalDate
    ): LiveData<Map<LocalDate, Float>> {

        val zoneId = ZoneId.of("Asia/Ho_Chi_Minh")
        val startTimestamp =
            Timestamp(startTime.with(firstDayOfMonth()).atStartOfDay(zoneId).toEpochSecond(), 0)
        val endTimestamp =
            Timestamp(endTime.with(lastDayOfMonth()).atTime(LocalTime.MAX).toEpochSecond(ZoneOffset.UTC), 0)

        Log.d(TAG, "startTime: ${startTime.with(firstDayOfMonth())}")
        Log.d(TAG, "endTime: ${endTime.with(lastDayOfMonth())}")
//        Log.d(TAG, "startTimeStamp: $startTimestamp")
//        Log.d(TAG, "endTimeStamp: $endTimestamp")

        val dataMap = HashMap<LocalDate, Float>()
        db.collection("Rental")
            .whereGreaterThanOrEqualTo("returnDate", startTimestamp)
            .whereLessThanOrEqualTo("returnDate", endTimestamp)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val rentalReturnTimestamp = document.get("returnDate") as Timestamp
                    val rentalReturnDate =
                        rentalReturnTimestamp.toDate().toInstant().atZone(zoneId).toLocalDate()
                            .withDayOfMonth(15)

                    val totalPaymentAtCurrentLoop = dataMap[rentalReturnDate]

                    if (totalPaymentAtCurrentLoop == null) {
                        val totalPaymentAsDouble = document.getDouble("totalPayment")
                        dataMap[rentalReturnDate] = totalPaymentAsDouble?.toFloat() ?: 0f
                    } else {
                        val totalPaymentAsDouble = document.getDouble("totalPayment")
                        dataMap[rentalReturnDate] =
                            (totalPaymentAsDouble?.toFloat() ?: 0f) + totalPaymentAtCurrentLoop
                    }
//                    Log.d(TAG, "Time: $rentalReturnDate - payment: ${dataMap[rentalReturnDate]} ")
//                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
        Log.d(TAG, dataMap.toString())
        return MutableLiveData(dataMap)
    }
}