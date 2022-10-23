package com.haidoan.android.ceedee.ui.disk_screen.disks

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.databinding.FragmentDisksBinding

import kotlinx.android.synthetic.main.activity_main.*

class DisksFragment : Fragment() {

    private var _binding: FragmentDisksBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDisksBinding.inflate(inflater, container, false)
        createMenu()
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
                    //menu.getItem(R.id.menu_disk_screen_filter).isVisible=false
                    menu.findItem(R.id.menu_disk_tab_filter).isVisible=true
                    menu.findItem(R.id.menu_disk_screen_filter).isVisible = false
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    // Handle the menu selection
                    return when (menuItem.itemId) {
                        R.id.menu_disk_screen_cart -> {
                            Log.d("TAG_MENU", "DISK_CART")
                            true
                        }
                        R.id.menu_disk_tab_filter -> {
                            Log.d("TAG_MENU", "DISK_FILTER")
                            true
                        }
                        R.id.menu_disk_screen_search -> {
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