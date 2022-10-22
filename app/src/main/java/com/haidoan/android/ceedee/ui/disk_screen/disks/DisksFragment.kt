package com.haidoan.android.ceedee.ui.disk_screen.disks

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.databinding.FragmentDisksBinding
import kotlinx.android.synthetic.main.activity_main.*

class DisksFragment : Fragment() {

    private var _binding: FragmentDisksBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDisksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvDisk.text = "DISK TAB FRAGMENT"
        addListeners()
    }
    override fun onResume() {
        super.onResume()
        addListeners()
    }
    private fun addListeners(){
        requireActivity().toolbar.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_disk_screen_cart -> {
                    Log.d("TAG_MENU","DISK_CART")
                    true
                }
                R.id.menu_disk_screen_filter -> {
                    Log.d("TAG_MENU","DISK_FILTER")
                    true
                }
                R.id.menu_disk_screen_search -> {
                    Log.d("TAG_MENU","DISK_SEARCH")
                    true
                }
                else -> false
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}