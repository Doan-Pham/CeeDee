package com.haidoan.android.ceedee.ui.disk_screen.disks

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.DiskStatus
import com.haidoan.android.ceedee.databinding.DiskStatusItemBinding
import com.haidoan.android.ceedee.databinding.FragmentDiskTabDisksBinding
import com.haidoan.android.ceedee.ui.disk_screen.repository.DiskStatusRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response

class DiskStatusAdapter(
    private val context: Context,
    private val diskViewModel: DiskViewModel,
    private val viewLifecycleOwner: LifecycleOwner,
    private val diskAdapter: DiskAdapter,
    private val fragmentDisksTabBinding: FragmentDiskTabDisksBinding?
) :
    ListAdapter<DiskStatus, DiskStatusAdapter.DiskStatusViewHolder>(DiskStatusUtils())
{

    private val displayedDiskStatus = arrayListOf<DiskStatus>()

    private var selectedItemPos = 0
    private var lastItemSelectedPos = 0

    override fun submitList(newList: MutableList<DiskStatus>?) {
        super.submitList(newList!!.toList())
        displayedDiskStatus.clear()
        displayedDiskStatus.addAll(newList.toList())
    }

    fun getItemAt(position: Int): DiskStatus {
        return displayedDiskStatus[position]
    }

    fun getAllGenres() : ArrayList<DiskStatus> {
        return displayedDiskStatus
    }

    inner class DiskStatusViewHolder(
        val binding: DiskStatusItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                setPosItemClick()

                if (getItemAt(bindingAdapterPosition).id == DiskStatusRepository.defaultDiskStatus) {
                    getAllDisks()
                } else {
                    getDiskFilterByStatus(displayedDiskStatus[bindingAdapterPosition].name)
                }
            }
        }

        private fun getDiskFilterByStatus(status: String) {
            val progressBar = fragmentDisksTabBinding?.progressbarDisk
            val rcvDiskTitle = fragmentDisksTabBinding?.rcvDisk
            diskViewModel.getDiskByDiskStatus(status)
                .observe(viewLifecycleOwner) { response ->
                    when (response) {
                        is Response.Loading -> {
                            progressBar?.visibility =
                                View.VISIBLE
                            progressBar?.visibility = View.INVISIBLE
                        }
                        is Response.Success -> {
                            val list = response.data
                            progressBar?.visibility =
                                View.GONE
                            rcvDiskTitle?.visibility = View.VISIBLE
                            diskAdapter.setFilterByDiskStatusList(list)
                            diskAdapter.setAllDiskFilterByDiskStatus(list)
                        }
                        is Response.Failure -> {
                            println(response.errorMessage)
                            progressBar?.visibility =
                                View.GONE
                            rcvDiskTitle?.visibility = View.VISIBLE
                        }
                    }

                }
        }

        private fun getAllDisks() {
            val progressBar = fragmentDisksTabBinding?.progressbarDisk
            val rcvDisk = fragmentDisksTabBinding?.rcvDisk
            diskViewModel.getDisks().observe(viewLifecycleOwner) { response ->
                when (response) {
                    is Response.Loading -> {
                        progressBar?.visibility =
                            View.VISIBLE
                        progressBar?.visibility = View.INVISIBLE
                    }
                    is Response.Success -> {
                        val list = response.data
                        progressBar?.visibility =
                            View.GONE
                        rcvDisk?.visibility = View.VISIBLE

                        diskAdapter.setFilterByDiskStatusList(list)
                        diskAdapter.setAllDiskFilterByDiskStatus(list)
                    }
                    is Response.Failure -> {
                        print(response.errorMessage)
                        progressBar?.visibility =
                            View.GONE
                        rcvDisk?.visibility = View.VISIBLE
                    }
                    else -> print(response.toString())
                }
            }
        }

        private fun setPosItemClick() {
            selectedItemPos = bindingAdapterPosition
            if (lastItemSelectedPos == -1)
                lastItemSelectedPos = selectedItemPos
            else {
                notifyItemChanged(lastItemSelectedPos)
                lastItemSelectedPos = selectedItemPos
            }
            notifyItemChanged(selectedItemPos)
        }

        fun setData(item: DiskStatus) {
            binding.apply {
                tvStatus.text = item.name
            }
        }

        fun setDefaultBackGround() {
            binding.cardStatus.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.light_grey
                )
            )
            binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.dark_grey))
        }

        fun setSelectedBackGround() {
            binding.cardStatus.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.primary
                )
            )
            binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
        }

    }

    override fun getItemCount(): Int {
        return displayedDiskStatus.size
    }

    private class DiskStatusUtils : DiffUtil.ItemCallback<DiskStatus>() {
        override fun areItemsTheSame(oldItem: DiskStatus, newItem: DiskStatus): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: DiskStatus, newItem: DiskStatus): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiskStatusViewHolder {
        val binding =
            DiskStatusItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiskStatusViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiskStatusViewHolder, position: Int) {
        holder.setData(displayedDiskStatus[position])
        holder.setIsRecyclable(true)

        if (position == selectedItemPos)
            holder.setSelectedBackGround()
        else
            holder.setDefaultBackGround()
    }
}