package com.haidoan.android.ceedee.ui.disk_screen.disks

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.databinding.FragmentDiskTabDisksBinding

import kotlinx.android.synthetic.main.activity_main.*

class DisksTabFragment : Fragment() {

    private var _binding: FragmentDiskTabDisksBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDiskTabDisksBinding.inflate(inflater, container, false)
        //createMenu()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDisk.text = "DISK TAB FRAGMENT"

    }

    private fun createMenu() {
        requireActivity().toolbar.addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    // Add menu items here
                    menu.clear()
                    menuInflater.inflate(R.menu.menu_disks,menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    // Handle the menu selection
                    return when (menuItem.itemId) {
                        R.id.menu_disks_cart -> {
                            Log.d("TAG_MENU", "DISK_CART")
                            true
                        }
                        R.id.menu_disks_tab_filter -> {
                            Log.d("TAG_MENU", "DISK_FILTER")
                            true
                        }
                        R.id.menu_disks_search -> {
                            Log.d("TAG_MENU", "DISK_SEARCH")
                            true
                        }
                        else -> false
                    }
                }
            }, viewLifecycleOwner, Lifecycle.State.RESUMED
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}