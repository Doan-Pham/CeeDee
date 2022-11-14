package com.haidoan.android.ceedee.ui.disk_screen.disk_requisition

import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.data.DiskTitle
import com.haidoan.android.ceedee.data.Requisition
import com.haidoan.android.ceedee.databinding.FragmentDiskRequisitionBinding


class DiskRequisitionFragment : Fragment() {

    private var _binding: FragmentDiskRequisitionBinding? = null
    private lateinit var requisitionAdapter: DiskRequisitionAdapter
    private val fakeDate = MutableLiveData(
        listOf(
            Requisition("", "Sup1", "sup@", listOf(DiskTitle())),
            Requisition("", "Sup2", "sup@", listOf(DiskTitle())),
            Requisition("", "Sup3", "sup@", listOf(DiskTitle()))
        )
    )

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDiskRequisitionBinding.inflate(inflater, container, false)
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
        fakeDate.observe(viewLifecycleOwner) { data -> requisitionAdapter.submitList(data) }
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