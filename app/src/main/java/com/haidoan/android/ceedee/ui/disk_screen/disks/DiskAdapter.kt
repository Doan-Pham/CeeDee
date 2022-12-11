package com.haidoan.android.ceedee.ui.disk_screen.disks

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.Disk
import com.haidoan.android.ceedee.data.DiskStatus
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.DiskItemBinding
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import java.util.*

@SuppressLint("NotifyDataSetChanged")
class DiskAdapter(
    private val context: Context,
    private val diskViewModel: DiskViewModel,
    private val viewLifecycleOwner: LifecycleOwner,
    private val diskTabFragment: DisksTabFragment
) :
    ListAdapter<Disk, DiskAdapter.DiskViewHolder>(DiskAdapter.DiskUtils()), Filterable {

    private val displayedDisk = arrayListOf<Disk>()
    private val allDisk = arrayListOf<Disk>()
    private val allDiskFilterByDiskStatus = arrayListOf<Disk>()

    override fun submitList(newList: MutableList<Disk>?) {
        super.submitList(newList!!.toList())
        allDisk.addAll(newList.toList())
        displayedDisk.clear()
        displayedDisk.addAll(newList.toList())
    }

    fun setAllDiskFilterByDiskStatus(newList: List<Disk>) {
        allDiskFilterByDiskStatus.clear()
        allDiskFilterByDiskStatus.addAll(newList.toList())
    }

    fun setFilterByDiskStatusList(newList: List<Disk>) {
        displayedDisk.clear()
        displayedDisk.addAll(newList)
        notifyDataSetChanged()
    }

    fun getListData(): ArrayList<Disk> {
        return allDisk
    }

    fun getItemAt(position: Int): Disk {
        return displayedDisk[position]
    }

    override fun getItemCount() = displayedDisk.size

    class DiskUtils : DiffUtil.ItemCallback<Disk>() {
        override fun areItemsTheSame(oldItem: Disk, newItem: Disk): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Disk, newItem: Disk): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiskViewHolder {
        val binding =
            DiskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiskViewHolder(binding, diskViewModel, viewLifecycleOwner, diskTabFragment)
    }

    override fun onBindViewHolder(holder: DiskViewHolder, position: Int) {
        holder.setData(displayedDisk[position])
        holder.setIsRecyclable(true)
    }

    inner class DiskViewHolder(
        private val binding: DiskItemBinding,
        private val diskViewModel: DiskViewModel,
        private val viewLifecycleOwner: LifecycleOwner,
        private val diskTabFragment: DisksTabFragment
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var adapterForSpinnerStatus: ArrayAdapter<DiskStatus>

        fun setData(item: Disk) {
            binding.apply {
                bindImage(imgDisk, item.status)

                tvDiskId.text = item.id
                tvDiskStatus.text = item.status
            }
        }

        private fun bindImage(imgView: ImageView, status: String) {
            when (status) {
                "In Store" -> {
                    imgView.load(R.drawable.ic_in_store) {
                        placeholder(R.drawable.ic_launcher)
                        error(R.drawable.ic_app_logo)
                    }
                }
                "Rented" -> {
                    imgView.load(R.drawable.ic_rented) {
                        placeholder(R.drawable.ic_launcher)
                        error(R.drawable.ic_app_logo)
                    }
                }
                else -> {
                    imgView.load(R.drawable.ic_damaged) {
                        placeholder(R.drawable.ic_launcher)
                        error(R.drawable.ic_app_logo)
                    }
                }
            }

        }

        init {
            binding.imgDiskBtnMore.setOnClickListener {
                val popupMenu = PopupMenu(context, binding.imgDiskBtnMore)
                popupMenu.menuInflater.inflate(
                    R.menu.popup_menu_disk_tab_more,
                    popupMenu.menu
                )
                popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.popup_disk_set_status -> {
                            setStatus()
                        }
                    }
                    true
                })
                popupMenu.show()
            }
        }

        private fun setStatus() {
            withSetStatus(itemView, LayoutInflater.from(context))
        }

        private fun makeToast(text: String) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }

        /**
         *  Create dialog for add set status
         * */
        @SuppressLint("MissingInflatedId")
        private fun withSetStatus(view: View, layoutInflater: LayoutInflater) {
            val builder = AlertDialog.Builder(context)
            val inflater = layoutInflater
            builder.setTitle("Set status")
            val dialogLayout = inflater.inflate(R.layout.dialog_set_status, null)
            val statusSpinner = dialogLayout.findViewById<Spinner>(R.id.spinner_set_status)
            builder.setView(dialogLayout)

            diskViewModel.getDiskStatus().observe(viewLifecycleOwner) { response ->
                when (response) {
                    is Response.Loading -> {
                    }
                    is Response.Success -> {
                        // init spinner
                        val list = response.data

                        adapterForSpinnerStatus = ArrayAdapter(
                            context,
                            android.R.layout.simple_spinner_dropdown_item,
                            list
                        )

                        binding.apply {
                            statusSpinner.apply {
                                adapter = adapterForSpinnerStatus
                            }
                        }

                        // add to fireStore onclick add
                        builder.setPositiveButton("ADD") { dialogInterface, i ->

                            if (statusSpinner.selectedItem.toString() == "") {
                                makeToast("Please fill all information!")
                            } else {
                                //change update status to firestore
                                val status = statusSpinner.selectedItem
                                updateDiskStatusToFireStore(status.toString())
                            }
                        }
                        builder.setNegativeButton("CANCEL") { dialogLayout, i -> }
                        builder.show()
                    }
                    is Response.Failure -> {
                        print(response.errorMessage)
                    }
                    else -> print(response.toString())
                }
            }

        }

        private fun updateDiskStatusToFireStore(status: String) {
            val disk = displayedDisk[bindingAdapterPosition]
            diskViewModel.updateDiskStatus(disk.id, status)
                .observe(viewLifecycleOwner) { response ->
                    when (response) {
                        is Response.Loading -> {
                        }
                        is Response.Success -> {
                            refresh()
                        }
                        is Response.Failure -> {
                            print(response.errorMessage)
                        }
                        else -> print(response.toString())
                    }

                }
        }

        private fun refresh() {
            diskTabFragment.init()
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = arrayListOf<Disk>()
                if (constraint == null || constraint.isEmpty()) {
                    filteredList.addAll(allDiskFilterByDiskStatus)
                } else {
                    val filterPattern: String =
                        constraint.toString().lowercase(Locale.getDefault()).trim()
                    allDiskFilterByDiskStatus.forEach { item ->
                        if (item.id.lowercase(Locale.getDefault()).trim()
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
                displayedDisk.clear()
                displayedDisk.addAll(results?.values as List<Disk>)
                notifyDataSetChanged()
            }
        }
    }
}