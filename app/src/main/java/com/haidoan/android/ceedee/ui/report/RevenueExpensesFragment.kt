package com.haidoan.android.ceedee.ui.report

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.databinding.FragmentReportBinding
import com.haidoan.android.ceedee.databinding.FragmentRevenueExpensesBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RevenueExpensesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RevenueExpensesFragment : Fragment() {

    private lateinit var binding: FragmentRevenueExpensesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRevenueExpensesBinding.inflate(inflater)
        return binding.root
    }

}