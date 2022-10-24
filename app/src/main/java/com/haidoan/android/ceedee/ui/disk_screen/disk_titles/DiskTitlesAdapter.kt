package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.haidoan.android.ceedee.R

import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.Genre
import com.haidoan.android.ceedee.databinding.DiskTitlesItemBinding
import com.haidoan.android.ceedee.utils.TypeUtils

class DiskTitlesAdapter(_diskTitlesViewModel: DiskTitlesViewModel,
                        _viewLifecycleOwner: LifecycleOwner
                        ) : RecyclerView.Adapter<DiskTitlesAdapter.DiskTitlesViewHolder>() {

    private lateinit var binding: DiskTitlesItemBinding

    private lateinit var iOnItemClickListener: IOnItemClickListener
    private lateinit var iOnItemMoreClickListener: IOnItemClickListener

    private var viewLifecycleOwner: LifecycleOwner
    private var diskTitlesViewModel: DiskTitlesViewModel

    init {
        diskTitlesViewModel=_diskTitlesViewModel
        viewLifecycleOwner=_viewLifecycleOwner
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiskTitlesViewHolder {
        binding = DiskTitlesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiskTitlesViewHolder(iOnItemClickListener, iOnItemMoreClickListener)
    }

    override fun onBindViewHolder(holder: DiskTitlesViewHolder, position: Int) {
        holder.setData(_differ.currentList[position])
        holder.setIsRecyclable(false)
    }

    fun sortByGenre(idHash: String) {
        val list: ArrayList<DiskTitle> = ArrayList()
        list.addAll(_differ.currentList)

        list.sortByDescending { it.genreId.hashCode().toString() == idHash
        }
        _differ.submitList(list)
    }

    fun sortByName(type: TypeUtils.SORT_BY_NAME) {
        val list: ArrayList<DiskTitle> = ArrayList()
        list.addAll(_differ.currentList)
        when (type) {
            TypeUtils.SORT_BY_NAME.Ascending -> {
                list.sortBy { it.name }
            }
            TypeUtils.SORT_BY_NAME.Descending -> {
                list.sortByDescending { it.name }
            }
        }
        _differ.submitList(list)
    }

    fun setIOnItemMoreClickListener(listener: IOnItemClickListener) {
        iOnItemMoreClickListener = listener
    }

    fun setIOnItemClickListener(listener: IOnItemClickListener) {
        iOnItemClickListener = listener
    }

    fun getItem(position: Int): DiskTitle {
        return _differ.currentList[position]
    }

    override fun getItemCount() = _differ.currentList.size

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
                diskTitlesViewModel.getDiskAmountInDiskTitlesFromFireStore(item.id).observe(viewLifecycleOwner) { response ->
                    when (response) {
                        is Response.Loading -> {
                            tvDiskTitlesAmount.text = "Loading..."
                        }
                        is Response.Success -> {
                            val it = response.data.count

                            Log.d("TAG_AMOUNT", "it "+ it.toString())
                            tvDiskTitlesAmount.text = "Amount: " + it.toString() + " CD"
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
                Log.d("TAG_ITEM", _differ.currentList[bindingAdapterPosition].name)
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

    private val differCallback = object : DiffUtil.ItemCallback<DiskTitle>() {
        override fun areItemsTheSame(oldItem: DiskTitle, newItem: DiskTitle): Boolean {
            return oldItem == newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: DiskTitle, newItem: DiskTitle): Boolean {
            return oldItem.id == newItem.id
        }

    }

    private val _differ = AsyncListDiffer(this, differCallback)
    fun differ(): AsyncListDiffer<DiskTitle> {
        return _differ
    }
}
