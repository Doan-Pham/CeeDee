package com.haidoan.android.ceedee

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.haidoan.android.ceedee.databinding.FragmentRentalBinding
import com.haidoan.android.ceedee.fragmentRentalTabs.Adapters.TabRentalAdapter
import com.haidoan.android.ceedee.fragmentRentalTabs.TabAll
import com.haidoan.android.ceedee.fragmentRentalTabs.TabComplete
import com.haidoan.android.ceedee.fragmentRentalTabs.TabInProgress
import com.haidoan.android.ceedee.fragmentRentalTabs.TabOverdue
import com.haidoan.android.ceedee.ui.login.AuthenticationViewModel



class RentalFragment : Fragment() {

    private var _binding: FragmentRentalBinding? = null
    private lateinit var fab:FloatingActionButton
    private lateinit var authViewModel: AuthenticationViewModel
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRentalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
      authViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]

        val viewPager=view.findViewById<ViewPager>(R.id.viewPagerRental)
        val tabRental=view.findViewById<TabLayout>(R.id.tabRental)
        val adapter= TabRentalAdapter(childFragmentManager)
        adapter.addFragment(TabAll(),"All")
        adapter.addFragment(TabInProgress(),"In progress")
        adapter.addFragment(TabOverdue(),"Overdue")
        adapter.addFragment(TabComplete(),"Complete")
        viewPager.adapter=adapter
       tabRental.setupWithViewPager(viewPager)
        fab=requireView().findViewById(R.id.fab)
        fab.setOnClickListener {
            findNavController().navigate(R.id.action_rentalFragment_to_newRentalScreen2)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    //- ------------------ TODO: test -----------------------------------------
    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
    }





}