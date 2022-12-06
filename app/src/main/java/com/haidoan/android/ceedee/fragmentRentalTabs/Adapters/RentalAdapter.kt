package com.haidoan.android.ceedee.fragmentRentalTabs.Adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.Rental
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class RentalAdapter(var rentalList: ArrayList<Rental>) :
    RecyclerView.Adapter<RentalAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.rental_item,
            parent, false
        )
        return MyViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentitem: Rental = rentalList[position]

        when (currentitem.rentalStatus) {
            "Complete" -> {
                holder.image.setImageResource(R.drawable.check)
            }
            "Overdue" -> {
                holder.image.setImageResource(R.drawable.multiply)
            }
            "In progress" -> {
                holder.image.setImageResource(R.drawable.clock)
            }
        }

        holder.customerName.text = currentitem.customerName
        holder.rentDate.text = convertToLocalDate(currentitem.rentDate)
        holder.dueDate.text = convertToLocalDate(currentitem.dueDate)
    }

    override fun getItemCount(): Int {
        return rentalList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateUserList(rentalList: List<Rental>) {

        this.rentalList.clear()
        this.rentalList.addAll(rentalList)
        notifyDataSetChanged()
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val customerName: TextView = itemView.findViewById(R.id.tvCustomerName)
        val image: ImageView = itemView.findViewById(R.id.imageviewItem)
        val rentDate: TextView = itemView.findViewById(R.id.tvRentDate)
        val dueDate: TextView = itemView.findViewById(R.id.tvDueDate)
    }

    fun convertToLocalDate(time: Timestamp?): String?{
        val zoneId = ZoneId.of("Asia/Ho_Chi_Minh")
        val dtf:DateTimeFormatter=DateTimeFormatter.ofPattern("dd/MMMM/YYYY")
       val local:LocalDate?=time?.toDate()?.toInstant()?.atZone(zoneId)?.toLocalDate()
        return local?.format(dtf)
    }

}
