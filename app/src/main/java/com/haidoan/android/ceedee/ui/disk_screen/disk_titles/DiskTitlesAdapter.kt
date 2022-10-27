package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.DiskTitlesItemBinding
import com.haidoan.android.ceedee.utils.TypeUtils


class DiskTitlesAdapter(
    _diskTitlesViewModel: DiskTitlesViewModel,
    _viewLifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<DiskTitlesAdapter.DiskTitlesViewHolder>() {

    private lateinit var binding: DiskTitlesItemBinding

    private val listData = ArrayList<DiskTitle>()

    private lateinit var iOnItemClickListener: IOnItemClickListener
    private lateinit var iOnItemMoreClickListener: IOnItemClickListener

    private var viewLifecycleOwner: LifecycleOwner
    private var diskTitlesViewModel: DiskTitlesViewModel

    private var mapDiskTitleAmount = hashMapOf<DiskTitle, Long>()

    init {
        diskTitlesViewModel = _diskTitlesViewModel
        viewLifecycleOwner = _viewLifecycleOwner
    }

    fun setListData(newList: ArrayList<DiskTitle>) {
        val diffCallback = DiskTitleDifferCallBack(listData, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
        listData.clear()
        listData.addAll(newList)
    }

    fun getListData(): ArrayList<DiskTitle> {
        return listData
    }

    fun sortByCDAmount(type: TypeUtils.SORT_BY_AMOUNT) {
        val listResult = arrayListOf<DiskTitle>()
        when (type) {
            TypeUtils.SORT_BY_AMOUNT.Ascending -> {
                val list = mapDiskTitleAmount.toList().sortedBy { it.second }
                list.forEach { listResult.add(it.first) }
            }
            TypeUtils.SORT_BY_AMOUNT.Descending -> {
                val list = mapDiskTitleAmount.toList().sortedByDescending { it.second }
                list.forEach { listResult.add(it.first) }
            }
        }
        setListData(listResult)
    }

    fun sortByName(type: TypeUtils.SORT_BY_NAME) {
        val list = arrayListOf<DiskTitle>()
        list.addAll(listData)
        when (type) {
            TypeUtils.SORT_BY_NAME.Ascending -> {
                list.sortBy { it.name }
            }
            TypeUtils.SORT_BY_NAME.Descending -> {
                list.sortByDescending { it.name }
            }
        }
        setListData(list)
    }

    fun setIOnItemMoreClickListener(listener: IOnItemClickListener) {
        iOnItemMoreClickListener = listener
    }

    fun setIOnItemClickListener(listener: IOnItemClickListener) {
        iOnItemClickListener = listener
    }

    fun getItem(position: Int): DiskTitle {
        return listData[position]
    }

    override fun getItemCount() = listData.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiskTitlesViewHolder {
        binding = DiskTitlesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiskTitlesViewHolder(iOnItemClickListener, iOnItemMoreClickListener)
    }

    override fun onBindViewHolder(holder: DiskTitlesViewHolder, position: Int) {
        holder.setData(listData[position])
        holder.setIsRecyclable(true)
    }

    inner class DiskTitlesViewHolder(
        listener: IOnItemClickListener,
        listenerMore: IOnItemClickListener
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

        init {
            itemView.setOnClickListener {
                listener.onItemClick(bindingAdapterPosition)
                Log.d("TAG_ITEM", listData[bindingAdapterPosition].name)
            }

            binding.imgDiskTitlesBtnMore.setOnClickListener {
                listenerMore.onItemClick((bindingAdapterPosition))
            }
        }
    }

    fun bindImage(imgView: ImageView, imgUrl: String?) {
        imgUrl?.let {
            val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
            imgView.load(imgUri) {
                placeholder(R.drawable.ic_launcher)
                error(R.drawable.ic_app_logo)
            }
        }
    }

   /*
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val list = arrayListOf<DiskTitle>()
                list.addAll(_differ.currentList)

                val filteredList = arrayListOf<DiskTitle>()
                if (constraint == null || constraint.isEmpty()) {
                    filteredList.addAll(list)
                } else {
                    val filterPattern = constraint.toString().toLowerCase().trim()

                    for (item in list) {
                        if (item.name.toLowerCase().contains(filterPattern)) {
                            filteredList.add(item)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                val list = arrayListOf<DiskTitle>()
                list.addAll(results?.values as ArrayList<DiskTitle>)
                list.forEach{it -> Log.d("TAG_FILTER_SEARCH","LIST: ${it.name}")}
                _differ.submitList(list)
            }
        }
    }*/
}
