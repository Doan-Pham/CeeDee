package com.haidoan.android.ceedee

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.haidoan.android.ceedee.databinding.DialogMonthYearPickerBinding
import java.util.*

class MonthYearPickerDialog(private val date: Date = Date()) : DialogFragment() {

    companion object {
        private const val MIN_YEAR = 2000
        private const val MAX_YEAR = 2099
    }

    private var title: String? = null
    fun setTitle(title: String) {
        this.title = title
    }

    private lateinit var binding: DialogMonthYearPickerBinding

    private var listener: DatePickerDialog.OnDateSetListener? = null

    fun setListener(listener: DatePickerDialog.OnDateSetListener?) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogMonthYearPickerBinding.inflate(requireActivity().layoutInflater)
        val cal: Calendar = Calendar.getInstance().apply { time = date }

        binding.pickerMonth.run {
            minValue = 1
            maxValue = 12
            value = cal.get(Calendar.MONTH) + 1
        }

        binding.pickerYear.run {
            val year = cal.get(Calendar.YEAR)
            minValue = MIN_YEAR
            maxValue = MAX_YEAR
            value = year
        }

        return AlertDialog.Builder(requireContext())
            .setTitle(title ?: "Please select month and year")
            .setView(binding.root)
            .setPositiveButton("Ok") { _, _ ->
                listener?.onDateSet(null, binding.pickerMonth.value, binding.pickerYear.value, 1)
            }
            .setNegativeButton("Cancel") { _, _ -> dialog?.cancel() }
            .create()
    }
}