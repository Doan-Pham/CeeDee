package com.haidoan.android.ceedee

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.haidoan.android.ceedee.data.USER_ROLE_CUSTOMER
import com.haidoan.android.ceedee.data.USER_ROLE_EMPLOYEE
import com.haidoan.android.ceedee.data.USER_ROLE_MANAGER
import com.haidoan.android.ceedee.data.User
import com.haidoan.android.ceedee.databinding.ActivityMainBinding
import com.haidoan.android.ceedee.ui.login.AuthenticationActivity
import com.haidoan.android.ceedee.ui.login.AuthenticationRepository
import com.haidoan.android.ceedee.ui.rental.fragment.NewRentalScreen
import com.haidoan.android.ceedee.ui.rental.fragment.NewRentalScreen.Companion.ARGUMENT_KEY_CUSTOMER_PHONE
import com.haidoan.android.ceedee.ui.rental.fragment.NewRentalScreen.Companion.ARGUMENT_KEY_IS_USER_CUSTOMER


private const val TAG = "MainActivity"
const val EXTRA_CURRENT_USER_PHONE_NUMBER = "EXTRA_CURRENT_USER_PHONE_NUMBER"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val appBarConfiguration = AppBarConfiguration(
        setOf(
            R.id.rentalFragment,
            R.id.diskFragment,
            R.id.reportFragment,
            R.id.userManagementFragment
        )
    )
    private val viewModel: MainActivityViewModel by viewModels {
        MainActivityViewModel.Factory(AuthenticationRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up navController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.mainContainer) as NavHostFragment
        navController = navHostFragment.navController

        //set up appbar
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        viewModel.currentUser.observe(this) {
            if (it != null) {
                Log.d(TAG, "viewModel.currentUser.observe() - currentUser: $it")
                setupBottomNav(it)
            }

        }
    }

    override fun onResume() {
        super.onResume()

        Log.d(TAG, "onResume Called")
        viewModel.resetUser()
    }

    private fun setupBottomNav(currentUser: User) {

        binding.bottomNavigationViewEmployee.visibility = View.GONE
        binding.bottomNavigationViewManager.visibility = View.GONE

        var bottomNavigationView: BottomNavigationView = binding.bottomNavigationViewEmployee
        // Set up bottomNav
        when (currentUser.role) {
            USER_ROLE_CUSTOMER -> {
                val newRentalScreen = NewRentalScreen()
                val newRentalScreenArguments = Bundle()

                newRentalScreenArguments.putBoolean(ARGUMENT_KEY_IS_USER_CUSTOMER, true)
                newRentalScreenArguments.putString(
                    ARGUMENT_KEY_CUSTOMER_PHONE, intent.getStringExtra(
                        EXTRA_CURRENT_USER_PHONE_NUMBER
                    )
                )
                newRentalScreen.arguments = newRentalScreenArguments

                supportFragmentManager.popBackStackImmediate()
                val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.mainContainer, newRentalScreen, NewRentalScreen.TAG)
                transaction.commit()
                return
            }
            USER_ROLE_EMPLOYEE -> bottomNavigationView = binding.bottomNavigationViewEmployee
            USER_ROLE_MANAGER -> bottomNavigationView = binding.bottomNavigationViewManager
        }
        Log.d(TAG, "setupBottomNav() - currentUser: ${viewModel.currentUser}")

        setupWithNavController(bottomNavigationView, navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNavigationView.visibility =
                if (appBarConfiguration.topLevelDestinations.contains(destination.id)) View.VISIBLE
                else View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.menu_item_main_sign_out -> {
                viewModel.signOut()
                startActivity(Intent(this, AuthenticationActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}