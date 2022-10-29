package fragmentRentalTabs

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.haidoan.android.ceedee.R
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [tabAll.newInstance] factory method to
 * create an instance of this fragment.
 */
private lateinit var viewModel : RentalViewModel
private lateinit var rentalRecyclerView: RecyclerView
lateinit var rental_adapter: RentalAdapter
lateinit var rentalList:ArrayList<Rental>
lateinit var tempRentalList:ArrayList<Rental>

class tabAll : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        tempRentalList= arrayListOf<Rental>()
        rentalList= arrayListOf<Rental>()
      getUserData(tempRentalList)
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_tab_all, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_rental,menu)
        val item=menu?.findItem(R.id.action_search)
        val searchView=item?.actionView as SearchView
        searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                TODO("Not yet implemented")
            }

            override fun onQueryTextChange(newText: String?): Boolean {
              rentalList.clear()
                val searchText=newText!!.lowercase(Locale.getDefault())
                if(searchText.isNotEmpty())
                {
                    tempRentalList.forEach{
                        if(it.customerId!!.lowercase(Locale.getDefault()).contains(searchText))
                        {
                            rentalList.add(it)
                        }
                    }
                    rentalRecyclerView.adapter!!.notifyDataSetChanged()
                }else
                {
                    rentalList.clear()
                    rentalList.addAll(tempRentalList)
                    rentalRecyclerView.adapter!!.notifyDataSetChanged()
                }
                return false
            }

        })
        super.onCreateOptionsMenu(menu, inflater)
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
    fun getUserData(temp:kotlin.collections.ArrayList<Rental>)
    {
       var databaseReference : DatabaseReference = FirebaseDatabase.getInstance().getReference("Rental")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val _rentalList : ArrayList<Rental> = snapshot.children.map { dataSnapshot ->
                        dataSnapshot.getValue(Rental::class.java)!!
                    } as ArrayList<Rental>
                    Log.d("Realtime success", "Thanh cong")
                    temp.addAll(_rentalList)

                } catch (e: Exception) {
                    Log.e("Realtime error", e.message.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Realtime error", error.message.toString())
            }
        })
    }
}

