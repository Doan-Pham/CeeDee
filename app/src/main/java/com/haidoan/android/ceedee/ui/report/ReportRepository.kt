package com.haidoan.android.ceedee.ui.report

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

private const val TAG = "ReportRepository.kt"

class ReportRepository(private val firestoreApi: FirestoreApi) {

    suspend fun getRevenueBetweenMonths(
        startTime: LocalDate,
        endTime: LocalDate
    ): LiveData<Map<LocalDate, Float>> {
        return withContext(Dispatchers.IO) {
            firestoreApi.getRevenueBetweenMonths(startTime, endTime)
        }
    }

    suspend fun getExpensesBetweenMonths(
        startTime: LocalDate,
        endTime: LocalDate
    ): LiveData<Map<LocalDate, Float>> {
        return withContext(Dispatchers.IO) {
            firestoreApi.getExpensesBetweenMonths(startTime, endTime)
        }
    }
}