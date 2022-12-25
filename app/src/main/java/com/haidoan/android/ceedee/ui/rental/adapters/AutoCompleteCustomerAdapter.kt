package com.haidoan.android.ceedee.ui.rental.adapters
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import com.haidoan.android.ceedee.data.customer.Customer
import com.haidoan.android.ceedee.databinding.ItemAutoTextviewCustomerBinding

import java.util.*

class AutoCompleteCustomerAdapter(
    context: Context,
    private val resource: Int,
    private val customerList: List<Customer>
) : ArrayAdapter<Customer>(context, resource, customerList), Filterable {

    private val customers = arrayListOf<Customer>()


    fun setListCustomers(list: List<Customer>) {
        customers.clear()
        customers.addAll(list)

    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val binding = ItemAutoTextviewCustomerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        val customer = customerList[position]
        binding.tvCustomerName.text = customer.fullName
        binding.tvCustomerAddress.text= customer.address
        binding.tvCustomerPhone.text= customer.phone

        return LayoutInflater.from(parent.context).inflate(
            resource, binding.root
        )
    }

    override fun getFilter(): Filter {
        return object: Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = arrayListOf<Customer>()
                if (constraint == null || constraint.isEmpty()) {
                    filteredList.addAll(customers)
                } else {
                    val filterPattern: String =
                        constraint.toString().lowercase(Locale.getDefault()).trim()
                    customers.forEach { item ->
                        if (item.phone.lowercase(Locale.getDefault()).trim()
                                .contains(filterPattern)
                        ) {
                            filteredList.add(item)
                        }
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                clear()
                addAll(results?.values as List<Customer>)
                notifyDataSetChanged()
            }

        }
    }

}
