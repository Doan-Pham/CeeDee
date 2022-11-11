package com.haidoan.android.ceedee.data.report

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

private const val TAG = "ReportRepository.kt"

class ReportRepository(private val firestoreApi: FirestoreStatisticsDataSource) {

    suspend fun getRevenueBetweenMonths(
        startTime: LocalDate,
        endTime: LocalDate
    ): Flow<Map<LocalDate, Float>> = firestoreApi.getRevenueBetweenMonths(startTime, endTime)


    suspend fun getExpensesBetweenMonths(
        startTime: LocalDate,
        endTime: LocalDate
    ): Flow<Map<LocalDate, Float>> = firestoreApi.getExpensesBetweenMonths(startTime, endTime)

    suspend fun getDiskAmountGroupByGenre(): Flow<Map<String, Int>> =
        firestoreApi.getDiskAmountGroupByGenre()

    suspend fun getDiskAmountGroupByStatus(): Flow<Map<String, Int>> =
        firestoreApi.getDiskAmountGroupByStatus()
}