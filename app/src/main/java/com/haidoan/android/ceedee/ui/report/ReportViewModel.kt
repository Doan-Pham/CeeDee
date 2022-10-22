package com.haidoan.android.ceedee.ui.report

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

private const val TAG = "ReportViewModel.kt"

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

    init {
        refreshMonthlyRevenue()
        refreshMonthlyExpenses()
    }

    private fun refreshMonthlyRevenue(
    ) {
        viewModelScope.launch {
            _monthlyRevenue.value =
                reportRepository.getRevenueBetweenMonths(startTime, endTime).value
        }
        Log.d(
            TAG,
            "Called refreshMonthlyRevenue(), revenue between $startTime and $endTime after refresh: ${_monthlyRevenue.value.toString()}"
        )
    }

    private fun refreshMonthlyExpenses(
    ) {
        viewModelScope.launch {
            _monthlyExpenses.value =
                reportRepository.getExpensesBetweenMonths(startTime, endTime).value
        }
        Log.d(
            TAG,
            "Called refreshMonthlyExpenses(), expenses between $startTime and $endTime after refresh: ${_monthlyExpenses.value.toString()}"
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


