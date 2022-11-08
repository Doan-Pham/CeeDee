package com.haidoan.android.ceedee.fragmentRentalTabs

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.fragmentRentalTabs.Adapters.RentalAdapter
import com.haidoan.android.ceedee.fragmentRentalTabs.ViewModels.RentalViewModel
import java.util.*
import kotlin.collections.ArrayList

private lateinit var viewModel: RentalViewModel
private lateinit var rentalRecyclerView: RecyclerView
private lateinit var rental_adapter: RentalAdapter
private lateinit var rentalList: ArrayList<Rental>
private lateinit var tempRentalList: ArrayList<Rental>

class TabAll : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        tempRentalList = arrayListOf<Rental>()
        rentalList = arrayListOf<Rental>()
        getUserData(tempRentalList)
        createMenu()
        return inflater.inflate(R.layout.fragment_tab_all, container, false)
    }
    private fun createMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_rental, menu)
                val item = menu?.findItem(R.id.action_search)
                val searchView = item?.actionView as SearchView
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(p0: String?): Boolean {
                        TODO("Not yet implemented")
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        rentalList.clear()
                        val searchText = newText!!.lowercase(Locale.getDefault())
                        if (searchText.isNotEmpty()) {
                            tempRentalList.forEach {
                                if (it.customerId!!.lowercase(Locale.getDefault()).contains(searchText)) {
                                    rentalList.add(it)
                                }
                            }
                            rentalRecyclerView.adapter!!.notifyDataSetChanged()
                        } else {
                            rentalList.clear()
                            rentalList.addAll(tempRentalList)
                            rentalRecyclerView.adapter!!.notifyDataSetChanged()
                        }
                        return false
                    }

                })

            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rentalRecyclerView = view.findViewById(R.id.tabAllRecyclerView)
        rentalRecyclerView.layoutManager = LinearLayoutManager(context)
        rentalRecyclerView.setHasFixedSize(true)
        rental_adapter = RentalAdapter(rentalList)
        rentalRecyclerView.adapter = rental_adapter
        viewModel = ViewModelProvider(this).get(RentalViewModel::class.java)
        viewModel.allRentals.observe(viewLifecycleOwner) {
            rental_adapter.updateUserList(it)
        }
    }

    fun getUserData(temp: kotlin.collections.ArrayList<Rental>) {
        val dbf: FirebaseFirestore = FirebaseFirestore.getInstance()
        val _rentalList: ArrayList<Rental> = arrayListOf<Rental>()
        dbf.collection("Rental").addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Log.e("Fire store error", error.message.toString())
                    return
                }
                for (dc: DocumentChange in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        _rentalList.add(dc.document.toObject(Rental::class.java))
                    }
                }
                    temp.addAll(_rentalList)
                    return
            }

        })


    }
}

