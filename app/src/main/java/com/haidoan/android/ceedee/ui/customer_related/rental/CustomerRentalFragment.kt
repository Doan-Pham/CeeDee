package com.haidoan.android.ceedee.ui.customer_related.rental

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.haidoan.android.ceedee.databinding.FragmentCustomerRentalBinding

class CustomerRentalFragment : Fragment() {

    private var _binding: FragmentCustomerRentalBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val customerRentalViewModel =
            ViewModelProvider(this).get(CustomerRentalViewModel::class.java)

        _binding = FragmentCustomerRentalBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        customerRentalViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}