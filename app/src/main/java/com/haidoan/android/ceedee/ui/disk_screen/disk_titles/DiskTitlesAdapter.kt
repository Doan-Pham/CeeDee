package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.haidoan.android.ceedee.R

import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.DiskTitlesItemBinding

class DiskTitlesAdapter : RecyclerView.Adapter<DiskTitlesAdapter.DiskTitlesViewHolder>() {

    private lateinit var binding: DiskTitlesItemBinding

    private lateinit var iOnItemClickListener: IOnItemClickListener
    private lateinit var iOnItemMoreClickListener: IOnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiskTitlesViewHolder {
        binding = DiskTitlesItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiskTitlesViewHolder(iOnItemClickListener,iOnItemMoreClickListener)
    }

    override fun onBindViewHolder(holder: DiskTitlesViewHolder, position: Int) {
        holder.setData(_differ.currentList[position])
        holder.setIsRecyclable(false)
    }

    fun setIOnItemMoreClickListener(listener: IOnItemClickListener){
        iOnItemMoreClickListener = listener
    }

    fun setIOnItemClickListener(listener: IOnItemClickListener){
        iOnItemClickListener = listener
    }

    fun getItem(position: Int): DiskTitle {
        return _differ.currentList[position]
    }

    override fun getItemCount() = _differ.currentList.size

    inner class DiskTitlesViewHolder(listener: IOnItemClickListener,
                                     listenerMore: IOnItemClickListener) : RecyclerView.ViewHolder(binding.root) {
        fun setData(item: DiskTitle) {
            binding.apply {
                bindImage(imgDiskTitlesCoverImg,item.coverImageUrl)
                tvDiskTitlesAmount.text = item.author
                tvDiskTitlesGenre.text = item.genreId
                tvDiskTitlesName.text = item.name
            }
        }

        init {
            itemView.setOnClickListener {
                listener.onItemClick(bindingAdapterPosition)
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
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: DiskTitle, newItem: DiskTitle): Boolean {
            return oldItem == newItem
        }

    }

    private val _differ = AsyncListDiffer(this, differCallback)
    fun differ(): AsyncListDiffer<DiskTitle> {
        return _differ
    }
}
