package com.haidoan.android.ceedee.ui.customer_related

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.haidoan.android.ceedee.R
import com.haidoan.android.ceedee.databinding.ActivityCustomerBinding
import com.haidoan.android.ceedee.ui.login.AuthenticationActivity
import com.haidoan.android.ceedee.ui.login.AuthenticationRepository

class CustomerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerBinding
    private val appBarConfiguration = AppBarConfiguration(
        setOf(
            R.id.customer_disk_fragment, R.id.customer_rental_fragment
        )
    )
    private val viewModel: CustomerActivityViewModel by viewModels {
        CustomerActivityViewModel.Factory(AuthenticationRepository(application))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_customer) as NavHostFragment).navController

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)

        binding.bottomNav.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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