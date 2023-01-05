package com.haidoan.android.ceedee.ui.customer_related.disk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.haidoan.android.ceedee.databinding.FragmentCustomerDiskBinding

class CustomerDiskFragment : Fragment() {

    private var _binding: FragmentCustomerDiskBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val customerDiskViewModel =
            ViewModelProvider(this).get(CustomerDiskViewModel::class.java)

        _binding = FragmentCustomerDiskBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        customerDiskViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}