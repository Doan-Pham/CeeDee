package com.haidoan.android.ceedee.data.report

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters.firstDayOfMonth
import java.time.temporal.TemporalAdjusters.lastDayOfMonth

private const val TAG = "FirestoreApi.kt"

// The zoneId is currently Asia/Ho_Chi_Minh which has an offset of 7 hours
private var DEFAULT_TIMEZONE_OFFSET_IN_HOURS = 7

class FirestoreApi {
    private val databaseRef = Firebase.firestore

    suspend fun getRevenueBetweenMonths(
        startTime: LocalDate,
        endTime: LocalDate,
    ): LiveData<Map<LocalDate, Float>> {

        val zoneId = ZoneId.of("Asia/Ho_Chi_Minh")

        val startTimestamp =
            Timestamp(
                startTime.with(firstDayOfMonth()).atStartOfDay(zoneId).toEpochSecond(),
                0
            )
        val endTimestamp =
            Timestamp(
                endTime.with(lastDayOfMonth()).atTime(LocalTime.MAX)
                    .toEpochSecond(ZoneOffset.ofHours(DEFAULT_TIMEZONE_OFFSET_IN_HOURS)),
                0
            )

        val monthlyRevenue = HashMap<LocalDate, Float>()

        val firebaseQueryRef = databaseRef.collection("Rental")
            .whereGreaterThanOrEqualTo("returnDate", startTimestamp)
            .whereLessThanOrEqualTo("returnDate", endTimestamp)
        val firebaseQueryAsTask: Task<QuerySnapshot> = firebaseQueryRef.get()

        for (document in firebaseQueryAsTask.await().documents) {
            val currentMonthAsTimestamp = document.get("returnDate") as Timestamp
            val currentMonthAsLocalDate =
                currentMonthAsTimestamp.toDate().toInstant().atZone(zoneId).toLocalDate()
                    .with(firstDayOfMonth())

            val revenueAtCurrentMonth = monthlyRevenue[currentMonthAsLocalDate]
            val currentRentalRevenue = document.getDouble("totalPayment")

            monthlyRevenue[currentMonthAsLocalDate] =
                (currentRentalRevenue?.toFloat() ?: 0f) + (revenueAtCurrentMonth ?: 0f)
        }
        Log.d(
            TAG,
            "Called getRevenueBetweenMonths(), revenue between $startTime and $endTime : $monthlyRevenue"
        )
        return MutableLiveData(monthlyRevenue)
    }

    suspend fun getExpensesBetweenMonths(
        startTime: LocalDate,
        endTime: LocalDate,
    ): LiveData<Map<LocalDate, Float>> {

        val zoneId = ZoneId.of("Asia/Ho_Chi_Minh")

        val startTimestamp =
            Timestamp(
                startTime.with(firstDayOfMonth()).atStartOfDay(zoneId).toEpochSecond(),
                0
            )
        val endTimestamp =
            Timestamp(
                endTime.with(lastDayOfMonth()).atTime(LocalTime.MAX)
                    .toEpochSecond(ZoneOffset.ofHours(DEFAULT_TIMEZONE_OFFSET_IN_HOURS)),
                0
            )

        val monthlyExpenses = HashMap<LocalDate, Float>()

        val firebaseQueryRef = databaseRef.collection("Import")
            .whereGreaterThanOrEqualTo("date", startTimestamp)
            .whereLessThanOrEqualTo("date", endTimestamp)
        val firebaseQueryAsTask: Task<QuerySnapshot> = firebaseQueryRef.get()

        for (document in firebaseQueryAsTask.await().documents) {
            val currentMonthAsTimestamp = document.get("date") as Timestamp
            val currentMonthAsLocalDate =
                currentMonthAsTimestamp.toDate().toInstant().atZone(zoneId).toLocalDate()
                    .with(firstDayOfMonth())

            val expensesAtCurrentMonth = monthlyExpenses[currentMonthAsLocalDate]
            val currentRentalExpenses = document.getDouble("totalPayment")

            monthlyExpenses[currentMonthAsLocalDate] =
                (currentRentalExpenses?.toFloat() ?: 0f) + (expensesAtCurrentMonth ?: 0f)
        }
        Log.d(
            TAG,
            "Called getExpensesBetweenMonths(), expenses between $startTime and $endTime : $monthlyExpenses"
        )
        return MutableLiveData(monthlyExpenses)
    }
}
