package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar

import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.Genre
import com.haidoan.android.ceedee.databinding.FragmentDiskTabDiskTitlesBinding

import com.haidoan.android.ceedee.databinding.GenreItemBinding
import com.haidoan.android.ceedee.ui.disk_screen.repository.GenreRepository
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response

class GenreAdapter(
    private val context: Context,
    private val diskTitlesViewModel: DiskTitlesViewModel,
    private val viewLifecycleOwner: LifecycleOwner,
    private val diskTitlesAdapter: DiskTitlesAdapter?,
    private val fragmentDiskTitlesBinding: FragmentDiskTabDiskTitlesBinding?
) :
    ListAdapter<Genre, GenreAdapter.GenreViewHolder>(GenreUtils())
    {

    private val displayedGenres = arrayListOf<Genre>()

   // private lateinit var diskTitlesAdapter: DiskTitlesAdapter
    private var selectedItemPos = -1
    private var lastItemSelectedPos = -1

    override fun submitList(newList: MutableList<Genre>?) {
        super.submitList(newList!!.toList())
        displayedGenres.clear()
        displayedGenres.addAll(newList.toList())
    }

  /*  fun setDiskTitlesAdapter(adapter: DiskTitlesAdapter) {
        diskTitlesAdapter = adapter
    }*/

    fun getItemAt(position: Int): Genre {
        return displayedGenres[position]
    }

    fun getAllGenres() : ArrayList<Genre> {
        return displayedGenres
    }

    inner class GenreViewHolder(
        val binding: GenreItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                setPosItemClick()
                Log.d("TAG", getItemAt(position = bindingAdapterPosition).name.toString())

                if (getItemAt(bindingAdapterPosition).id == GenreRepository.defaultGenre) {
                    getAllDiskTitle()
                } else {
                    getDiskTitleFilterByGenreId(displayedGenres[bindingAdapterPosition].id)
                }

            }
        }

        private fun getDiskTitleFilterByGenreId(id: String) {
            val progressBar = fragmentDiskTitlesBinding?.progressbarDiskTitle
            val rcvDiskTitle = fragmentDiskTitlesBinding?.rcvDiskTitles
            diskTitlesViewModel.getDiskTitleFilterByGenreId(id)
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
                            diskTitlesAdapter?.setFilterByGenreList(list)
                            diskTitlesAdapter?.setAllDiskTitleFilterByGenre(list)
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

        private fun getAllDiskTitle() {
            val progressBar = fragmentDiskTitlesBinding?.progressbarDiskTitle
            val rcvDiskTitle = fragmentDiskTitlesBinding?.rcvDiskTitles
            diskTitlesViewModel.getDiskTitles().observe(viewLifecycleOwner) { response ->
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
                        diskTitlesAdapter?.setFilterByGenreList(list)
                        diskTitlesAdapter?.setAllDiskTitleFilterByGenre(list)
                    }
                    is Response.Failure -> {
                        print(response.errorMessage)
                        progressBar?.visibility =
                            View.GONE
                        rcvDiskTitle?.visibility = View.VISIBLE
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

        fun setData(item: Genre) {
            binding.apply {
                tvGenre.text = item.name
            }
        }

        fun setDefaultBackGround() {
            binding.cardGenre.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.light_grey
                )
            )
            binding.tvGenre.setTextColor(ContextCompat.getColor(context, R.color.dark_grey))
        }

        fun setSelectedBackGround() {
            binding.cardGenre.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.primary
                )
            )
            binding.tvGenre.setTextColor(ContextCompat.getColor(context, R.color.white))
        }

    }

    override fun getItemCount(): Int {
        return displayedGenres.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val binding =
            GenreItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GenreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        holder.setData(getItemAt(position))
        holder.setIsRecyclable(true)

        if (position == selectedItemPos)
            holder.setSelectedBackGround()
        else
            holder.setDefaultBackGround()

    }

    private class GenreUtils : DiffUtil.ItemCallback<Genre>() {
        override fun areItemsTheSame(oldItem: Genre, newItem: Genre): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Genre, newItem: Genre): Boolean {
            return oldItem.id == newItem.id
        }
    }
}