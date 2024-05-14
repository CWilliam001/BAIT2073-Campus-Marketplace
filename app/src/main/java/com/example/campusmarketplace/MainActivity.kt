package com.example.campusmarketplace


import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.campusmarketplace.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    // Declare variables
    private lateinit var appBarConfig : AppBarConfiguration
    private lateinit var navController : NavController
    private lateinit var navBottomView : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Get drawer layout and navigation view references
        navBottomView = binding.bottomNavView

        // Get the navController from the nav host fragment
        navController = findNavController(R.id.nav_host_fragment)

        // Configure the app bar to toggle drawer icon and up icon accordingly
        // ONLY FOR THOSE FRAGMENT AT THE TOP LEVEL ONLY
        appBarConfig = AppBarConfiguration(
            setOf(R.id.nav_login,  R.id.nav_home, R.id.nav_profile, R.id.nav_seller)
        )

        navBottomView.setupWithNavController(navController)

        supportActionBar?.hide()

        navController.addOnDestinationChangedListener {
            _, destination, _ ->

            if (destination.id in listOf(R.id.nav_home, R.id.nav_profile, R.id.nav_seller)) {
                navBottomView.visibility = View.VISIBLE
            } else {
                navBottomView.visibility = View.GONE
            }
        }

        //Setup the ActionBar with the NavController
        setupActionBarWithNavController(navController, appBarConfig)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
    }
}

