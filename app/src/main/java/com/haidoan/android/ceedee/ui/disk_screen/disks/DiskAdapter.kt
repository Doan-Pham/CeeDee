package com.haidoan.android.ceedee.ui.disk_screen.disks

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.get
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.Disk
import com.haidoan.android.ceedee.data.DiskStatus
import com.haidoan.android.ceedee.data.Genre
import com.haidoan.android.ceedee.databinding.DiskItemBinding
import com.haidoan.android.ceedee.ui.disk_screen.disk_add_edit.DiskAddEditViewModel
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class DiskAdapter(
    private val context: Context,
    private val diskViewModel: DiskViewModel,
    private val viewLifecycleOwner: LifecycleOwner
) :
    ListAdapter<Disk, DiskAdapter.DiskViewHolder>(DiskAdapter.DiskUtils()) {

    private val displayedDisk = arrayListOf<Disk>()
    private val allDisk = arrayListOf<Disk>()

    override fun submitList(newList: MutableList<Disk>?) {
        super.submitList(newList!!.toList())
        allDisk.addAll(newList.toList())
        displayedDisk.clear()
        displayedDisk.addAll(newList.toList())
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
        return DiskViewHolder(binding, diskViewModel, viewLifecycleOwner)
    }

    override fun onBindViewHolder(holder: DiskViewHolder, position: Int) {
        holder.setData(displayedDisk[position])
        holder.setIsRecyclable(true)
    }

    inner class DiskViewHolder(
        private val binding: DiskItemBinding,
        private val diskViewModel: DiskViewModel,
        private val viewLifecycleOwner: LifecycleOwner
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var adapterForSpinnerStatus: ArrayAdapter<DiskStatus>

        fun setData(item: Disk) {
            binding.apply {
                bindImage(imgDisk)

                tvDiskId.text = item.id

                /*  val triggerTime: LocalDateTime = LocalDateTime.ofInstant(
                      Instant.ofEpochMilli(item.importDate.seconds * 1000),
                      TimeZone.getDefault().toZoneId()
                  )*/
                tvDiskStatus.text = item.status
            }
        }

        private fun bindImage(imgView: ImageView) {
            imgView.load(R.drawable.ic_in_store) {
                placeholder(R.drawable.ic_launcher)
                error(R.drawable.ic_app_logo)
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
                                //makeToast("Please fill all information!")
                            } else {
                                Log.d("TAG_SPINNER", statusSpinner.selectedItem.toString())
                                //change update status to firestore
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
    }

}