package com.haidoan.android.ceedee

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.findFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.haidoan.android.ceedee.databinding.FragmentFirstBinding
import com.haidoan.android.ceedee.ui.login.AuthenticationViewModel
import fragmentRentalTabs.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    private lateinit var authViewModel: AuthenticationViewModel
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
      authViewModel = ViewModelProvider(this)[AuthenticationViewModel::class.java]
       binding.buttonLogout.setOnClickListener{
          authViewModel.signOut()
            val i = Intent(requireActivity(), LoginActivity::class.java)
           startActivity(i)
      }
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