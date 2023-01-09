package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.ItemDiskTitlePopularBinding

@SuppressLint("NotifyDataSetChanged")
class PopularAdapter(
    private val context: Context,
    private val genreAdapter: GenreAdapter
) :
    ListAdapter<DiskTitle, PopularAdapter.PopularViewHolder>(
        PopularUtils()
    ) {

    private val displayedDiskTitles = arrayListOf<DiskTitle>()

    override fun submitList(newList: MutableList<DiskTitle>?) {
        super.submitList(newList!!.toList())
        displayedDiskTitles.clear()
        displayedDiskTitles.addAll(newList.toList())
    }

    fun getItemAt(position: Int): DiskTitle {
        return displayedDiskTitles[position]
    }

    inner class PopularViewHolder(
        val binding: ItemDiskTitlePopularBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener { view ->
                val bundle = getBundleDiskTitle()
                view.findNavController().navigate(R.id.diskDetailsFragment, bundle)
            }
        }

        fun setData(item: DiskTitle) {
            binding.apply {
                tvPopularName.text = item.name
                tvPopularName.text = item.author
            }

            bindImage(binding.imgPopularItem, item.coverImageUrl)
        }

        private fun bindImage(imgView: ImageView, imgUrl: String?) {
            imgUrl?.let {
                val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
                imgView.load(imgUri) {
                    placeholder(R.drawable.ic_disk_cover_placeholder_96)
                    error(R.drawable.ic_disk_cover_placeholder_96)
                    crossfade(true)
                }
            }
        }

        private fun getBundleDiskTitle(): Bundle {
            val diskTitle = getItemAt(bindingAdapterPosition)

            val listGenre = genreAdapter.getAllGenres()
            lateinit var genre: String
            for (item in listGenre) {
                if (item.id == diskTitle.genreId) {
                    genre = item.name
                    break
                }
            }

            return bundleOf(
                "disk_title" to diskTitle,
                "genre_name" to genre
            )
        }

    }

    private class PopularUtils : DiffUtil.ItemCallback<DiskTitle>() {
        override fun areItemsTheSame(oldItem: DiskTitle, newItem: DiskTitle): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: DiskTitle, newItem: DiskTitle): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularViewHolder {
        val binding =
            ItemDiskTitlePopularBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PopularViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PopularViewHolder, position: Int) {
        holder.setData(displayedDiskTitles[position])
        holder.setIsRecyclable(true)
    }
}