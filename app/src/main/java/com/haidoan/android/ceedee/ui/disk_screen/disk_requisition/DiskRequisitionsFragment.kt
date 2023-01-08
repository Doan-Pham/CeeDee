package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsFirestoreDataSource
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsRepository
import com.haidoan.android.ceedee.databinding.FragmentDiskRequisitionsBinding
import com.haidoan.android.ceedee.ui.disk_screen.DiskFragmentDirections

private const val TAG = "DiskRequisitionsFrag"

class DiskRequisitionsFragment : Fragment() {

    private var _binding: FragmentDiskRequisitionsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val viewModel: DiskRequisitionsViewModel by lazy {
        ViewModelProvider(
            this, DiskRequisitionsViewModel.Factory(
                DiskRequisitionsRepository(DiskRequisitionsFirestoreDataSource())
            )
        )[DiskRequisitionsViewModel::class.java]
    }
    private lateinit var requisitionAdapter: DiskRequisitionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDiskRequisitionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpOptionMenu()

        requisitionAdapter =
            DiskRequisitionAdapter(onButtonImportClick = { requisition ->
                val action =
                    DiskFragmentDirections.actionDiskFragmentToDiskImportFragment(
                        requisition.id
                    )
                findNavController().navigate(action)
            })

        binding.apply {
            recyclerviewRequisition.adapter = requisitionAdapter
            recyclerviewRequisition.layoutManager = LinearLayoutManager(activity)
            chipGroupFilter.setOnCheckedStateChangeListener { group, _ ->
                when (group.checkedChipId) {
                    R.id.chip_filter_by_pending -> viewModel.setFilteringCategory(
                        DiskRequisitionFilterCategory.FILTER_BY_PENDING
                    )
                    R.id.chip_filter_by_completed -> viewModel.setFilteringCategory(
                        DiskRequisitionFilterCategory.FILTER_BY_COMPLETED
                    )
                    R.id.chip_filter_by_all -> viewModel.setFilteringCategory(
                        DiskRequisitionFilterCategory.FILTER_BY_ALL
                    )
                }
                Log.d(TAG, "CheckId change: ${group.checkedChipId}")
            }
        }

        viewModel.requisitions.observe(viewLifecycleOwner) { requisitions ->
            requisitionAdapter.submitList(
                requisitions
            )
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun setUpOptionMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_disk_requisition, menu)

                val searchView: SearchView =
                    (menu.findItem(R.id.menu_item_disk_requisition_search).actionView as SearchView)
                searchView.queryHint = "Type here to search"
                searchView.maxWidth = Int.MAX_VALUE

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.searchRequisition(newText)
                        return false
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_item_disk_requisition_search -> {
                        true
                    }
                    R.id.menu_item_disk_requisition_import -> {
                        navigateToNewRequisitionFragment()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun navigateToNewRequisitionFragment() {
        val action =
            DiskFragmentDirections.actionDiskFragmentToNewRequisitionFragment()
        findNavController().navigate(action)
    }
}