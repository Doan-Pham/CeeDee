package fragmentRentalTabs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.haidoan.android.ceedee.R

class RentalAdapter(var rentalList:ArrayList<Rental>): RecyclerView.Adapter<RentalAdapter.MyViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.rental_item,
            parent,false
        )
        return MyViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentitem:Rental = rentalList[position]

       when(currentitem.status)
       {
           "Complete"->{holder.image.setImageResource(R.drawable.check)}
           "Overdue"->{holder.image.setImageResource(R.drawable.multiply)}
           "In progress"->{holder.image.setImageResource(R.drawable.clock)}
       }
        holder.customerName.text=currentitem.customerId
        holder.rentDate.text=currentitem.rentDate
        holder.dueDate.text = currentitem.dueDate
    }

    override fun getItemCount(): Int {
        return rentalList.size
    }

    fun updateUserList(rentalList : List<Rental>){

        this.rentalList.clear()
        this.rentalList.addAll(rentalList)
        notifyDataSetChanged()
    }

    class  MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val customerName : TextView = itemView.findViewById(R.id.tvCustomerName)
        val image:ImageView=itemView.findViewById(R.id.imageviewItem)
        val rentDate : TextView = itemView.findViewById(R.id.tvRentDate)
        val dueDate : TextView = itemView.findViewById(R.id.tvDueDate)
    }

}
