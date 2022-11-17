package com.haidoan.android.ceedee.ui.disk_screen.disk_titles

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.databinding.DiskTitlesItemBinding
import com.haidoan.android.ceedee.databinding.FragmentDiskTabDiskTitlesBinding
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

    private val mapDiskTitleAmount = hashMapOf<DiskTitle, Long>()

    init {
    }

    override fun submitList(newList: MutableList<DiskTitle>?) {
        super.submitList(newList!!.toList())
        allDiskTitles.addAll(newList.toList())
        displayedDiskTitles.clear()
        displayedDiskTitles.addAll(newList.toList())
    }

    fun setAllDiskTitleFilterByGenre(newList: List<DiskTitle>) {
        allDiskTitleFilterByGenre.clear()
        allDiskTitleFilterByGenre.addAll(newList.toList())
    }

    fun setDiskTitlesTabFragment(fragment: DiskTitlesTabFragment) {
        diskTitlesTabFragment = fragment;
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

    fun getListData(): ArrayList<DiskTitle> {
        return allDiskTitles
    }

    fun setFilterByGenreList(newList: List<DiskTitle>) {
        displayedDiskTitles.clear()
        displayedDiskTitles.addAll(newList)
        notifyDataSetChanged()
    }

    fun sortByCDAmount(type: TypeUtils.SORT_BY_AMOUNT) {
        val sortByAmountList = arrayListOf<DiskTitle>()
        when (type) {
            TypeUtils.SORT_BY_AMOUNT.Ascending -> {
                val list = mapDiskTitleAmount.toList().sortedBy { it.second }
                list.forEach { map ->
                    displayedDiskTitles.forEach { item ->
                        if (map.first.id == item.id) {
                            sortByAmountList.add(item)
                        }
                    }
                }
            }
            TypeUtils.SORT_BY_AMOUNT.Descending -> {
                val list = mapDiskTitleAmount.toList().sortedByDescending { it.second }
                list.forEach { map ->
                    displayedDiskTitles.forEach { item ->
                        if (map.first.id == item.id) {
                            sortByAmountList.add(item)
                        }
                    }
                }
            }
        }
        displayedDiskTitles.clear()
        displayedDiskTitles.addAll(sortByAmountList)
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
                popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
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
            val amount = bundle.getLong("amount_disk_title")
            val diskTitle = bundle.customGetSerializable<DiskTitle>("disk_title")

            if (amount > 0) {
                Toast.makeText(
                    context,
                    "Can't be delete disk title because it contain disks",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                diskTitlesViewModel.deleteDiskTitle(diskTitle!!.id)
                    .observe(viewLifecycleOwner) { response ->
                        when (response) {
                            is Response.Loading -> {

                            }
                            is Response.Success -> {
                                diskTitlesTabFragment.init();
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
                "amount_disk_title" to mapDiskTitleAmount[diskTitle],
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
