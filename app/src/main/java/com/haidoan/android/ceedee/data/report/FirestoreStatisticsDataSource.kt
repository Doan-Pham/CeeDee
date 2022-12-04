package com.haidoan.android.ceedee.data.report

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.haidoan.android.ceedee.data.Genre
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.TemporalAdjusters.firstDayOfMonth
import java.time.temporal.TemporalAdjusters.lastDayOfMonth
import java.util.*

private const val TAG = "FSStatisticsDataSource"

// The zoneId is currently Asia/Ho_Chi_Minh which has an offset of 7 hours
private var DEFAULT_TIMEZONE_OFFSET_IN_HOURS = 7

class FirestoreStatisticsDataSource {
    private val databaseRef = Firebase.firestore

    suspend fun getRevenueBetweenMonths(
        startTime: LocalDate,
        endTime: LocalDate,
    ): Flow<Map<LocalDate, Float>> = callbackFlow {

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

        val monthlyRevenue = TreeMap<LocalDate, Float>()
        var iteratorLocalDate = startTime

        while (iteratorLocalDate.isBefore(endTime) || iteratorLocalDate.isEqual(endTime)) {
            monthlyRevenue[iteratorLocalDate] = 0f
            iteratorLocalDate = iteratorLocalDate.plusMonths(1)
        }

        val firebaseQueryRef = databaseRef.collection("Rental")
            .whereGreaterThanOrEqualTo("returnDate", startTimestamp)
            .whereLessThanOrEqualTo("returnDate", endTimestamp)

        // Registers callback to firestore, which will be called on new events
        val subscription = firebaseQueryRef.addSnapshotListener { snapshot, _ ->
            if (snapshot == null) {
                return@addSnapshotListener
            }
            monthlyRevenue.clear()
            for (document in snapshot.documents) {
                val currentMonthAsTimestamp = document.get("returnDate") as Timestamp
                val currentMonthAsLocalDate =
                    currentMonthAsTimestamp.toDate().toInstant().atZone(zoneId).toLocalDate()
                        .with(firstDayOfMonth())

                val revenueAtCurrentMonth = monthlyRevenue[currentMonthAsLocalDate]
                val currentRentalRevenue = document.getDouble("totalPayment")

                monthlyRevenue[currentMonthAsLocalDate] =
                    (currentRentalRevenue?.toFloat() ?: 0f) + (revenueAtCurrentMonth ?: 0f)
            }
            // Sends events to the flow! Consumers will get the new events
            try {
                trySend(monthlyRevenue).isSuccess
            } catch (e: Throwable) {
                // Event couldn't be sent to the flow
            }
        }

        // The callback inside awaitClose will be executed when the flow is
        // either closed or cancelled.
        // In this case, remove the callback from Firestore
        awaitClose { subscription.remove() }
        Log.d(
            TAG,
            "Called getRevenueBetweenMonths(), revenue between $startTime and $endTime : $monthlyRevenue"
        )
    }

    suspend fun getExpensesBetweenMonths(
        startTime: LocalDate,
        endTime: LocalDate,
    ): Flow<Map<LocalDate, Float>> = callbackFlow {

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

        val monthlyExpenses = TreeMap<LocalDate, Float>()
        var iteratorLocalDate = startTime

        while (iteratorLocalDate.isBefore(endTime) || iteratorLocalDate.isEqual(endTime)) {
            monthlyExpenses[iteratorLocalDate] = 0f
            iteratorLocalDate = iteratorLocalDate.plusMonths(1)
        }

        val firebaseQueryRef = databaseRef.collection("Import")
            .whereGreaterThanOrEqualTo("date", startTimestamp)
            .whereLessThanOrEqualTo("date", endTimestamp)

        // Registers callback to firestore, which will be called on new events
        val subscription = firebaseQueryRef.addSnapshotListener { snapshot, _ ->
            if (snapshot == null) {
                return@addSnapshotListener
            }

            monthlyExpenses.clear()
            for (document in snapshot.documents) {
                val currentMonthAsTimestamp = document.get("date") as Timestamp
                val currentMonthAsLocalDate =
                    currentMonthAsTimestamp.toDate().toInstant().atZone(zoneId).toLocalDate()
                        .with(firstDayOfMonth())

                val expensesAtCurrentMonth = monthlyExpenses[currentMonthAsLocalDate]
                val currentRentalExpenses = document.getDouble("totalPayment")

                monthlyExpenses[currentMonthAsLocalDate] =
                    (currentRentalExpenses?.toFloat() ?: 0f) + (expensesAtCurrentMonth ?: 0f)
            }
            // Sends events to the flow! Consumers will get the new events
            try {
                trySend(monthlyExpenses).isSuccess
            } catch (e: Throwable) {
                // Event couldn't be sent to the flow
            }
        }

        // The callback inside awaitClose will be executed when the flow is
        // either closed or cancelled.
        // In this case, remove the callback from Firestore
        awaitClose { subscription.remove() }
        Log.d(
            TAG,
            "Called getExpensesBetweenMonths(), expenses between $startTime and $endTime : $monthlyExpenses"
        )
    }

    suspend fun getDiskAmountGroupByGenre() = callbackFlow<Map<String, Int>> {
        // Using TreeMap with data class as key causes app to crash, USE HASHMAP INSTEAD!!
        val diskAmountGroupByGenre = HashMap<Genre, Int>()
        for (document in databaseRef.collection("Genre").get().await().documents) {
            diskAmountGroupByGenre[Genre(document.id, document.get("name") as String)] = 0
        }

        // Registers callback to firestore, which will be called on new events
        val subscription =
            databaseRef
                .collection("DiskTitle")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot == null) {
                        return@addSnapshotListener
                    }

                    for (document in snapshot.documents) {

                        val currentDiskGenreId = document.get("genreId") as String
                        val currentDiskTitleDiskAmount =
                            (document.get("diskAmount") as Long).toInt()

                        val currentDiskGenre =
                            diskAmountGroupByGenre.keys.first { it.id == currentDiskGenreId };

                        val diskAmountAtCurrentGenre = diskAmountGroupByGenre[currentDiskGenre] ?: 0
                        diskAmountGroupByGenre[currentDiskGenre] =
                            diskAmountAtCurrentGenre + currentDiskTitleDiskAmount
                    }
                    // Sends events to the flow! Consumers will get the new events
                    try {
                        trySend(diskAmountGroupByGenre.mapKeys { it.key.name }).isSuccess
                    } catch (e: Throwable) {
                        // Event couldn't be sent to the flow
                    }
                }

        // The callback inside awaitClose will be executed when the flow is
        // either closed or cancelled.
        // In this case, remove the callback from Firestore
        awaitClose { subscription.remove() }
        Log.d(
            TAG,
            "Called getDiskAmountGroupByGenre(), result : $diskAmountGroupByGenre"
        )
    }

    suspend fun getDiskAmountGroupByStatus() = callbackFlow<Map<String, Int>> {

        val diskAmountGroupByStatus = TreeMap<String, Int>()
        for (document in databaseRef.collection("DiskStatus").get().await().documents) {
            diskAmountGroupByStatus[document.get("name") as String] = 0
        }

        // Registers callback to firestore, which will be called on new events
        val subscription =
            databaseRef
                .collection("Disk")
                .orderBy("status", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot == null) {
                        return@addSnapshotListener
                    }
                    diskAmountGroupByStatus.clear()
                    for (document in snapshot.documents) {

                        val currentDiskStatus = document.get("status") as String

                        val diskAmountAtCurrentStatus =
                            diskAmountGroupByStatus[currentDiskStatus] ?: 0
                        diskAmountGroupByStatus[currentDiskStatus] = diskAmountAtCurrentStatus + 1
                    }
                    // Sends events to the flow! Consumers will get the new events
                    try {
                        trySend(diskAmountGroupByStatus).isSuccess
                    } catch (e: Throwable) {
                        // Event couldn't be sent to the flow
                    }
                }

        // The callback inside awaitClose will be executed when the flow is
        // either closed or cancelled.
        // In this case, remove the callback from Firestore
        awaitClose { subscription.remove() }
        Log.d(
            TAG,
            "Called getDiskAmountGroupByStatus(), result : $diskAmountGroupByStatus"
        )
    }

    suspend fun getTotalRentalGroupByGenre() = callbackFlow<Map<String, Int>> {

        val totalRentalsGroupByGenre = HashMap<Genre, Int>()
        for (document in databaseRef.collection("Genre").get().await().documents) {
            totalRentalsGroupByGenre[Genre(document.id, document.get("name") as String)] = 0
        }

        // Registers callback to firestore, which will be called on new events
        val subscription =
            databaseRef
                .collection("DiskTitle")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot == null) {
                        return@addSnapshotListener
                    }

                    for (document in snapshot.documents) {

                        val currentDiskGenreId = document.get("genreId") as String
                        val currentDiskTitleTotalRentalAmount =
                            (document.get("totalRentalAmount") as Long).toInt()

                        val currentDiskGenre =
                            totalRentalsGroupByGenre.keys.first { it.id == currentDiskGenreId }

                        val totalRentalAtCurrentGenre =
                            totalRentalsGroupByGenre[currentDiskGenre] ?: 0

                        totalRentalsGroupByGenre[currentDiskGenre] =
                            totalRentalAtCurrentGenre + currentDiskTitleTotalRentalAmount
                    }
                    // Sends events to the flow! Consumers will get the new events
                    try {
                        trySend(totalRentalsGroupByGenre.mapKeys { it.key.name }).isSuccess
                    } catch (e: Throwable) {
                        // Event couldn't be sent to the flow
                    }
                }

        // The callback inside awaitClose will be executed when the flow is
        // either closed or cancelled.
        // In this case, remove the callback from Firestore
        awaitClose { subscription.remove() }
        Log.d(
            TAG,
            "Called getTotalRentalGroupByGenre(), result : $totalRentalsGroupByGenre"
        )
    }
}

