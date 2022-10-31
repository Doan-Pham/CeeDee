package com.haidoan.android.ceedee

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.haidoan.android.ceedee.databinding.FragmentRentalBinding
import com.haidoan.android.ceedee.ui.login.AuthenticationViewModel
import fragmentRentalTabs.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class RentalFragment : Fragment() {

    private var _binding: FragmentRentalBinding? = null

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
    private lateinit var searchViewModel : SearchViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
      authViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]

        var viewPager=view.findViewById<ViewPager>(R.id.viewPagerRental)
        var tabRental=view.findViewById<TabLayout>(R.id.tabRental)
        var adapter=tabRentalAdapter(childFragmentManager)
        adapter.addFragment(tabAll(),"All")
        adapter.addFragment(tabInProgress(),"In progress")
        adapter.addFragment(tabOverdue(),"Overdue")
        adapter.addFragment(tabComplete(),"Complete")
        viewPager.adapter=adapter
       tabRental.setupWithViewPager(viewPager)

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