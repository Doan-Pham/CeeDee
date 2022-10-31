package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.DiskTitlesItemBinding
import com.haidoan.android.ceedee.utils.TypeUtils
import java.util.*

@SuppressLint("NotifyDataSetChanged")
class DiskTitlesAdapter: ListAdapter<DiskTitle, DiskTitlesAdapter.DiskTitlesViewHolder>(DiskTitleUtils()),
    Filterable {

    private val displayedDiskTitles = arrayListOf<DiskTitle>()
    private val allDiskTitles = arrayListOf<DiskTitle>()

    private lateinit var diskTitlesViewModel: DiskTitlesViewModel
    private lateinit var viewLifecycleOwner: LifecycleOwner
    private lateinit var iOnItemClickListener: IOnItemClickListener
    private lateinit var iOnItemMoreClickListener: IOnItemClickListener

    private val mapDiskTitleAmount = hashMapOf<DiskTitle, Long>()

    init {
    }

    override fun submitList(newList: MutableList<DiskTitle>?) {
        super.submitList(newList!!.toList())
        allDiskTitles.addAll(newList.toList())
        displayedDiskTitles.clear()
        displayedDiskTitles.addAll(newList.toList())
    }

    fun setLifecycleOwner(lco: LifecycleOwner){
        viewLifecycleOwner=lco
    }

    fun setDiskTitlesViewModel(viewModel: DiskTitlesViewModel){
        diskTitlesViewModel=viewModel
    }

    fun getListData(): ArrayList<DiskTitle> {
        return displayedDiskTitles
    }

    fun sortByCDAmount(type: TypeUtils.SORT_BY_AMOUNT) {
        displayedDiskTitles.clear()
        when (type) {
            TypeUtils.SORT_BY_AMOUNT.Ascending -> {
                val list = mapDiskTitleAmount.toList().sortedBy { it.second }
                list.forEach { displayedDiskTitles.add(it.first) }
            }
            TypeUtils.SORT_BY_AMOUNT.Descending -> {
                val list = mapDiskTitleAmount.toList().sortedByDescending { it.second }
                list.forEach { displayedDiskTitles.add(it.first) }
            }
        }
        notifyDataSetChanged()
    }

    fun sortByName(type: TypeUtils.SORT_BY_NAME) {
        when (type) {
            TypeUtils.SORT_BY_NAME.Ascending -> {
                displayedDiskTitles.sortBy { it.name }
            }
            TypeUtils.SORT_BY_NAME.Descending -> {
                displayedDiskTitles.sortByDescending { it.name }
            }
        }
        notifyDataSetChanged()
     /*   Log.d("TAG_SORTBYNAME", "sortByName in adapter")
        listResult.forEach {
            Log.d("TAG_SORTBYNAME", "${it.name}")
        }*/
    }

    fun setIOnItemMoreClickListener(listener: IOnItemClickListener) {
        iOnItemMoreClickListener = listener
    }

    fun setIOnItemClickListener(listener: IOnItemClickListener) {
        iOnItemClickListener = listener
    }

    fun getItemAt(position: Int): DiskTitle {
        return displayedDiskTitles[position]
    }

    override fun getItemCount() = displayedDiskTitles.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiskTitlesViewHolder {
        val binding =
            DiskTitlesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiskTitlesViewHolder(
            binding = binding,
            viewLifecycleOwner = viewLifecycleOwner,
            diskTitlesViewModel = diskTitlesViewModel,
            itemClickListeners = iOnItemClickListener,
            moreBtnClickListeners = iOnItemMoreClickListener
        )
    }

    override fun onBindViewHolder(holder: DiskTitlesViewHolder, position: Int) {
        holder.setData(displayedDiskTitles[position])
        holder.setIsRecyclable(true)
    }

    private class DiskTitleUtils : DiffUtil.ItemCallback<DiskTitle>() {
        override fun areItemsTheSame(oldItem: DiskTitle, newItem: DiskTitle): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: DiskTitle, newItem: DiskTitle): Boolean {
            return oldItem.id == newItem.id
        }
    }

    inner class DiskTitlesViewHolder(
        private val binding: DiskTitlesItemBinding,
        private val diskTitlesViewModel: DiskTitlesViewModel,
        private val viewLifecycleOwner: LifecycleOwner,
        private val itemClickListeners: IOnItemClickListener,
        private val moreBtnClickListeners: IOnItemClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        fun setData(item: DiskTitle) {
            binding.apply {
                bindImage(imgDiskTitlesCoverImg, item.coverImageUrl)

                tvDiskTitlesAuthor.text = item.author
                tvDiskTitlesName.text = item.name

                Log.d("TAG_AMOUNT", item.id)
                diskTitlesViewModel.getDiskAmountInDiskTitles(item.id)
                    .observe(viewLifecycleOwner) { response ->
                        when (response) {
                            is Response.Loading -> {
                                tvDiskTitlesAmount.text = "Loading..."
                            }
                            is Response.Success -> {
                                val it = response.data.count
                                mapDiskTitleAmount[item] = it
                                Log.d("TAG_AMOUNT", "it $it")
                                tvDiskTitlesAmount.text = "Amount: $it CD"
                            }
                            is Response.Failure -> {
                                tvDiskTitlesAmount.text = "Fail to get amount..."
                                print(response.errorMessage)
                            }
                        }

                    }
            }
        }

        private fun bindImage(imgView: ImageView, imgUrl: String?) {
            imgUrl?.let {
                val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
                imgView.load(imgUri) {
                    placeholder(R.drawable.ic_launcher)
                    error(R.drawable.ic_app_logo)
                }
            }
        }

        init {
            itemView.setOnClickListener {
                itemClickListeners.onItemClick(bindingAdapterPosition)
                Log.d("TAG_ITEM", displayedDiskTitles[bindingAdapterPosition].name)
            }

            binding.imgDiskTitlesBtnMore.setOnClickListener {
                moreBtnClickListeners.onItemClick((bindingAdapterPosition))
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = arrayListOf<DiskTitle>()
                if (constraint == null || constraint.isEmpty()) {
                    filteredList.addAll(allDiskTitles)
                } else {
                    val filterPattern: String =
                        constraint.toString().lowercase(Locale.getDefault()).trim()
                    allDiskTitles.forEach { item ->
                        if (item.name.lowercase(Locale.getDefault()).trim()
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
                displayedDiskTitles.clear()
                displayedDiskTitles.addAll(results?.values as List<DiskTitle>)
                notifyDataSetChanged()
            }
        }
    }
}
