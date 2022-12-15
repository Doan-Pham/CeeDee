package com.haidoan.android.ceedee.ui.rental.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.Rental
import com.haidoan.android.ceedee.databinding.ItemRentalBinding
import com.haidoan.android.ceedee.databinding.SectionHeaderRentalBinding
import io.github.luizgrp.sectionedrecyclerviewadapter.Section
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class RentalSection(
    private val currentMonth: String?,
    private val currentMonthRentals: List<Rental>,
    private val onButtonReturnClick: (Rental) -> Unit
) : Section(
    SectionParameters.builder()
        .itemResourceId(R.layout.item_rental)
        .headerResourceId(R.layout.section_header_rental)
        .build()
) {
    class RentalViewHolder(
        private val binding: ItemRentalBinding,
        val onButtonReturnClick: (Rental) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(rental: Rental) {
            binding.apply {

                when (rental.rentalStatus) {
                    "Complete" -> {
                        imageviewItem.setImageResource(R.drawable.check)
                        buttonReturnDisk.visibility = View.INVISIBLE
                    }
                    "Overdue" -> {
                        imageviewItem.setImageResource(R.drawable.multiply)
                        buttonReturnDisk.visibility = View.INVISIBLE
                    }
                    "In progress" -> {
                        imageviewItem.setImageResource(R.drawable.clock)
                        buttonReturnDisk.visibility = View.VISIBLE
                    }
                }
                tvCustomerName.text = rental.customerName
                tvRentDate.text = convertToLocalDate(rental.rentDate)
                tvDueDate.text = convertToLocalDate(rental.dueDate)

                buttonReturnDisk.setOnClickListener {
                    onButtonReturnClick(rental)
                }
            }
        }
    }

    class RentalMonthViewHolder(private val binding: SectionHeaderRentalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentMonth: String?) {
            binding.apply {
                textviewCurrentMonth.text = currentMonth
            }
        }
    }

    override fun getContentItemsTotal(): Int {
        return currentMonthRentals.size
    }

    override fun getItemViewHolder(view: View): RecyclerView.ViewHolder {
        val binding =
            ItemRentalBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        return RentalViewHolder(binding, onButtonReturnClick)
    }

    override fun onBindItemViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as RentalViewHolder).bind(currentMonthRentals[position])
    }

    override fun getHeaderViewHolder(view: View): RecyclerView.ViewHolder {
        val binding =
            SectionHeaderRentalBinding.inflate(
                LayoutInflater.from(view.context),
                view as ViewGroup,
                false
            )
        return RentalMonthViewHolder(binding)

    }

    override fun onBindHeaderViewHolder(holder: RecyclerView.ViewHolder) {
        (holder as RentalMonthViewHolder).bind(currentMonth)
    }
}

private fun convertToLocalDate(time: Timestamp?): String? {
    val zoneId = ZoneId.of("Asia/Ho_Chi_Minh")
    val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val localDate: LocalDate? = time?.toDate()?.toInstant()?.atZone(zoneId)?.toLocalDate()
    return dtf.format(localDate)
}