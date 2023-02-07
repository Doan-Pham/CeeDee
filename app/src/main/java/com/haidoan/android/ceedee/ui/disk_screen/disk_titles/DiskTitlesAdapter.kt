package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.DiskTitlesItemBinding
import com.haidoan.android.ceedee.ui.disk_screen.utils.Response
import com.haidoan.android.ceedee.ui.disk_screen.utils.TypeUtils
import java.io.Serializable
import java.util.*

@SuppressLint("NotifyDataSetChanged")
class DiskTitlesAdapter(private val context: Context) :
    ListAdapter<DiskTitle, DiskTitlesAdapter.DiskTitlesViewHolder>(DiskTitleUtils()),
    Filterable {

    private val displayedDiskTitles = arrayListOf<DiskTitle>()
    private val allDiskTitles = arrayListOf<DiskTitle>()
    private val allDiskTitleFilterByGenre = arrayListOf<DiskTitle>()

    private lateinit var diskTitlesViewModel: DiskTitlesViewModel
    private lateinit var viewLifecycleOwner: LifecycleOwner
    private lateinit var diskTitlesTabFragment: DiskTitlesTabFragment

    private lateinit var genreAdapter: GenreAdapter

    init {

    }

    override fun submitList(newList: MutableList<DiskTitle>?) {
        super.submitList(newList!!.toList())
        allDiskTitles.addAll(newList.toList())
        displayedDiskTitles.clear()
        displayedDiskTitles.addAll(newList.toList())
        notifyDataSetChanged()
    }

    fun setAllDiskTitleFilterByGenre(newList: List<DiskTitle>) {
        allDiskTitleFilterByGenre.clear()
        allDiskTitleFilterByGenre.addAll(newList.toList())
    }

    fun setFilterByGenreList(newList: List<DiskTitle>) {
        displayedDiskTitles.clear()
        displayedDiskTitles.addAll(newList)
        notifyDataSetChanged()
    }

    fun setDiskTitlesTabFragment(fragment: DiskTitlesTabFragment) {
        diskTitlesTabFragment = fragment
    }

    fun setGenreAdapter(g: GenreAdapter) {
        genreAdapter = g
    }

    fun setLifecycleOwner(lco: LifecycleOwner) {
        viewLifecycleOwner = lco
    }

    fun setDiskTitlesViewModel(viewModel: DiskTitlesViewModel) {
        diskTitlesViewModel = viewModel
    }

    fun sortByCDAmount(type: TypeUtils.SORT_BY_AMOUNT) {
        when (type) {
            TypeUtils.SORT_BY_AMOUNT.Ascending -> {
                displayedDiskTitles.sortBy { it.diskAmount }
            }
            TypeUtils.SORT_BY_AMOUNT.Descending -> {
                displayedDiskTitles.sortByDescending { it.diskAmount }
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
        )
    }

    override fun onBindViewHolder(holder: DiskTitlesViewHolder, position: Int) {
        holder.setData(displayedDiskTitles[position])
        //holder.setIsRecyclable(true)
    }

    class DiskTitleUtils : DiffUtil.ItemCallback<DiskTitle>() {
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
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun setData(item: DiskTitle) {
            binding.apply {
                bindImage(imgDiskTitlesCoverImg, item.coverImageUrl)

                tvDiskTitlesAuthor.text = item.author
                tvDiskTitlesName.text = item.name
                tvDiskTitlesAmount.text = "In store: ${item.diskInStoreAmount}/${item.diskAmount} CD"
            }
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

        init {
            itemView.setOnClickListener { view ->
                val bundle = getBundleDiskTitle()
                view.findNavController().navigate(R.id.diskDetailsFragment, bundle)
            }

            binding.imgDiskTitlesBtnMore.setOnClickListener {
                val popupMenu = PopupMenu(context, binding.imgDiskTitlesBtnMore)
                popupMenu.menuInflater.inflate(
                    R.menu.popup_menu_disk_title_tab_more_btn,
                    popupMenu.menu
                )
                popupMenu.setOnMenuItemClickListener({ item ->
                    when (item.itemId) {
                        R.id.popup_disk_title_add_to_import -> {
                            //TODO: Add to import
                        }
                        R.id.popup_disk_title_edit -> {
                            goToAddEditScreen()
                        }
                        R.id.popup_disk_title_delete -> {
                            deleteDiskTitle()
                        }
                    }
                    true
                })
                popupMenu.show()
            }
        }

        private fun deleteDiskTitle() {
            val bundle = getBundleDiskTitle()
            val diskTitle = bundle.customGetSerializable<DiskTitle>("disk_title")

            if (diskTitle!!.diskAmount > 0) {
                Toast.makeText(
                    context,
                    "Can't be delete disk title because it contain disks",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                displayAlertDialogDeleteDiskTitle(diskTitle)
            }
        }

        private fun displayAlertDialogDeleteDiskTitle(diskTitleNeedToDelete: DiskTitle?) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete this disk title?")

            builder.setPositiveButton("DELETE") { dialogInterface, i ->
                deleteDiskTitleFromFireStore(diskTitleNeedToDelete)
            }
            builder.setNegativeButton("CANCEL") { dialogLayout, i ->

            }
            builder.show()
        }

        private fun deleteDiskTitleFromFireStore(diskTitle: DiskTitle?) {
            diskTitlesViewModel.deleteDiskTitle(diskTitle!!.id)
                .observe(viewLifecycleOwner) { response ->
                    when (response) {
                        is Response.Loading -> {

                        }
                        is Response.Success -> {
                            diskTitlesTabFragment.init()
                            Toast.makeText(
                                context,
                                "Delete disk title success!",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                        is Response.Failure -> {

                        }
                    }
                }
        }

        private fun goToAddEditScreen() {
            val bundle = getBundleDiskTitle()
            itemView.findNavController().navigate(R.id.diskAddEditFragment, bundle)
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

    @Suppress("DEPRECATION")
    inline fun <reified T : Serializable> Bundle.customGetSerializable(key: String): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSerializable(key, T::class.java)
        } else {
            getSerializable(key) as? T
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = arrayListOf<DiskTitle>()
                if (constraint == null || constraint.isEmpty()) {
                    filteredList.addAll(allDiskTitleFilterByGenre)
                } else {
                    val filterPattern: String =
                        constraint.toString().lowercase(Locale.getDefault()).trim()
                    allDiskTitleFilterByGenre.forEach { item ->
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
