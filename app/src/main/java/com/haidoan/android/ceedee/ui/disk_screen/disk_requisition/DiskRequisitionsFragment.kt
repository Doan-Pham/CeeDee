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
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsFirestoreDataSource
import com.haidoan.android.ceedee.data.disk_requisition.DiskRequisitionsRepository
import com.haidoan.android.ceedee.databinding.FragmentDiskRequisitionsBinding

private const val TAG = "DiskRequisitionsFrag"

class DiskRequisitionsFragment : Fragment() {

    private var _binding: FragmentDiskRequisitionsBinding? = null
    private lateinit var requisitionAdapter: DiskRequisitionAdapter
    private val viewModel: DiskRequisitionsViewModel by lazy {
        ViewModelProvider(
            this, DiskRequisitionsViewModel.Factory(
                DiskRequisitionsRepository(DiskRequisitionsFirestoreDataSource())
            )
        )[DiskRequisitionsViewModel::class.java]
    }


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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

        requisitionAdapter = DiskRequisitionAdapter { }

        binding.apply {
            recyclerviewRequisition.adapter = requisitionAdapter
            recyclerviewRequisition.layoutManager = LinearLayoutManager(activity)
        }
        viewModel.requisitions.observe(viewLifecycleOwner) { requisitions ->
            Log.d(TAG, requisitions.toString())
            requisitionAdapter.submitList(
                requisitions
            )
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
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}