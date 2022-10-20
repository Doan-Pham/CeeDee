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
import java.util.*
import kotlin.collections.HashMap

private const val TAG = "FirestoreApi.kt"

class FirestoreApi {
    private val db = Firebase.firestore

    fun getRevenueBetweenMonths(
        startMonth: Int = 11,
        startYear: Int = 2022,
        endMonth: Int = 11,
        endYear: Int = 2022
    ): LiveData<Map<Date, Float>> {

        val startTime = LocalDate.of(startYear, startMonth, 1).with(firstDayOfMonth())
        val endTime = LocalDate.of(endYear, endMonth, 1).with(lastDayOfMonth())
        val startTimestamp =
            Timestamp(startTime.atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh")).toEpochSecond(), 0)
        val endTimestamp =
            Timestamp(endTime.atTime(LocalTime.MAX).toEpochSecond(ZoneOffset.UTC), 0)

        Log.d(TAG, "startTime: $startTime")
        Log.d(TAG, "endTime: $endTime")
        Log.d(TAG, "startTimeStamp: $startTimestamp")
        Log.d(TAG, "endTimeStamp: $endTimestamp")

        db.collection("Rental")
            .whereGreaterThanOrEqualTo("returnDate", startTimestamp)
            .whereLessThanOrEqualTo("returnDate", endTimestamp)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
        return MutableLiveData(
            mapOf(
                Date.from(
                    startTime.atStartOfDay(ZoneId.systemDefault()).toInstant()
                ) to 45f
            )
        )
    }
}