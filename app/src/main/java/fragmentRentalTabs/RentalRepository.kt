package fragmentRentalTabs

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*

class RentalRepository {

    private val databaseReference : DatabaseReference = FirebaseDatabase.getInstance().getReference("Rental")

    @Volatile private var inst : RentalRepository ?= null

    fun getInstance() : RentalRepository{
        return inst ?: synchronized(this){
            val instance = RentalRepository()
            inst = instance
            instance
        }

    }
    fun loadUsers(rentalList : MutableLiveData<ArrayList<Rental>>){
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val _rentalList : ArrayList<Rental> = snapshot.children.map { dataSnapshot ->
                        dataSnapshot.getValue(Rental::class.java)!!
                    } as ArrayList<Rental>
                    rentalList.postValue(_rentalList)
                    Log.d("Realtime success","Thanh cong")

                }catch (e : Exception){
                    Log.e("Realtime error",e.message.toString())
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Realtime error",error.message.toString())
            }
        })

    }

}