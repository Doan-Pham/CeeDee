package com.haidoan.android.ceedee.ui.report.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.haidoan.android.ceedee.data.report.ReportRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

private const val TAG = "ReportViewModel.kt"

enum class DiskDataGroupingCategory {
    DISK_AMOUNT_BY_GENRE,
    DISK_AMOUNT_BY_STATUS,
    TOTAL_RENTAL_BY_GENRE
}

class ReportViewModel(application: Application, private val reportRepository: ReportRepository) :
    AndroidViewModel(application) {

    private var startTime: LocalDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
    private var endTime: LocalDate = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth())

    private val _monthlyRevenue = MutableLiveData<Map<LocalDate, Float>>()
    val monthlyRevenue: LiveData<Map<LocalDate, Float>>
        get() = _monthlyRevenue

    private val _monthlyExpenses = MutableLiveData<Map<LocalDate, Float>>()
    val monthlyExpenses: LiveData<Map<LocalDate, Float>>
        get() = _monthlyExpenses


    private val _diskDataGroupingCategory = MutableLiveData(
        DiskDataGroupingCategory.DISK_AMOUNT_BY_GENRE
    )

    private val _diskRelatedData = _diskDataGroupingCategory.switchMap { groupingCategory ->
        val resultFromDataLayer = MutableLiveData<Map<String, Int>>()
        viewModelScope.launch {
            when (groupingCategory) {
                DiskDataGroupingCategory.DISK_AMOUNT_BY_GENRE ->
                    reportRepository
                        .getDiskAmountGroupByGenre()
                        .collect { data -> resultFromDataLayer.value = data }

                DiskDataGroupingCategory.DISK_AMOUNT_BY_STATUS ->
                    reportRepository
                        .getDiskAmountGroupByStatus()
                        .collect { data -> resultFromDataLayer.value = data }
                DiskDataGroupingCategory.TOTAL_RENTAL_BY_GENRE ->
                    reportRepository
                        .getTotalRentalGroupByGenre()
                        .collect { data -> resultFromDataLayer.value = data }
            }
        }
        resultFromDataLayer
    }
    val diskRelatedData: LiveData<Map<String, Int>>
        get() = _diskRelatedData

    init {
        refreshMonthlyRevenue()
        refreshMonthlyExpenses()
        refreshDiskRelatedData()
    }

    private fun refreshMonthlyRevenue(
    ) {
        viewModelScope.launch {
            reportRepository.getRevenueBetweenMonths(startTime, endTime).collect { revenue ->
                _monthlyRevenue.value = revenue
            }
        }
        Log.d(
            TAG,
            "Called refreshMonthlyRevenue(), revenue between $startTime and $endTime after refresh: ${_monthlyRevenue.value.toString()}"
        )
    }

    private fun refreshMonthlyExpenses(
    ) {
        viewModelScope.launch {
            reportRepository.getExpensesBetweenMonths(startTime, endTime)
                .collect { expenses -> _monthlyExpenses.value = expenses }
        }
        Log.d(
            TAG,
            "Called refreshMonthlyExpenses(), expenses between $startTime and $endTime after refresh: ${_monthlyExpenses.value.toString()}"
        )
    }

    private fun refreshDiskRelatedData(
    ) {
//        viewModelScope.launch {
//            reportRepository.getDiskAmountGroupByGenre()
//                .collect { data -> _diskRelatedData.value = data }
//        }
        Log.d(
            TAG,
            "Called refreshDiskRelatedData(), result: ${_diskRelatedData.value.toString()}"
        )
    }

    fun setMonthsPeriod(
        startTime: LocalDate = LocalDate.now(),
        endTime: LocalDate = LocalDate.now()
    ) {
        this.startTime = startTime.with(TemporalAdjusters.firstDayOfMonth())
        this.endTime = endTime.with(TemporalAdjusters.lastDayOfMonth())
        refreshMonthlyRevenue()
        refreshMonthlyExpenses()
    }

    fun setDiskDataGroupingCategory(category: DiskDataGroupingCategory) {
        _diskDataGroupingCategory.value = category
    }

    class Factory(val app: Application, private val reportRepository: ReportRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ReportViewModel(app, reportRepository) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}


