package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.Rental
import com.haidoan.android.ceedee.databinding.ItemRentalBinding
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class RentalAdapter(private val onButtonReturnClick: (Rental) -> Unit) :
    ListAdapter<Rental, RentalAdapter.RentalViewHolder>
        (RentalUtils()) {

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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RentalViewHolder {
        val binding =
            ItemRentalBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return RentalViewHolder(binding, onButtonReturnClick)
    }

    override fun onBindViewHolder(holder: RentalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class RentalUtils : DiffUtil.ItemCallback<Rental>() {
    override fun areItemsTheSame(oldItem: Rental, newItem: Rental): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Rental, newItem: Rental): Boolean {
        return oldItem.id == newItem.id
    }
}

private fun convertToLocalDate(time: Timestamp?): String? {
    val zoneId = ZoneId.of("Asia/Ho_Chi_Minh")
    val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val localDate: LocalDate? = time?.toDate()?.toInstant()?.atZone(zoneId)?.toLocalDate()
    return dtf.format(localDate)
}