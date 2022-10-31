package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.Genre
import com.haidoan.android.ceedee.databinding.GenreItemBinding

class GenreAdapter(private val context: Context) :
    ListAdapter<Genre, GenreAdapter.GenreViewHolder>(GenreUtils()) {

    private val displayedGenres = arrayListOf<Genre>()

    private var selectedItemPos = -1
    private var lastItemSelectedPos = -1

    override fun submitList(newList: MutableList<Genre>?) {
        super.submitList(newList!!.toList())
        displayedGenres.clear()
        displayedGenres.addAll(newList.toList())
    }

    fun getItemAt(position: Int): Genre {
        return displayedGenres[position]
    }

    inner class GenreViewHolder(
        val binding: GenreItemBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                selectedItemPos = bindingAdapterPosition
                if (lastItemSelectedPos == -1)
                    lastItemSelectedPos = selectedItemPos
                else {
                    notifyItemChanged(lastItemSelectedPos)
                    lastItemSelectedPos = selectedItemPos
                }
                notifyItemChanged(selectedItemPos)
            }
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