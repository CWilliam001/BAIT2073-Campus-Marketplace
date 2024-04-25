package com.example.campusmarketplace

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.example.campusmarketplace.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    // Declare variables
    private lateinit var drawerLayout : DrawerLayout
    private lateinit var navView : NavigationView
    private lateinit var appBarConfig : AppBarConfiguration
    private lateinit var navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // Get drawer layout and navigation view references
        drawerLayout = binding.drawerLayout
        navView = binding.navView

        // Get the navController from the nav host fragment
        navController = findNavController(R.id.nav_host_fragment)

        // Configure the app bar to toggle drawer icon and up icon accordingly
        // ONLY FOR THOSE FRAGMENT AT THE TOP LEVEL ONLY
        appBarConfig = AppBarConfiguration(setOf(R.id.nav_login, R.id.nav_home, R.id.nav_messageList, R.id.nav_aboutUs), drawerLayout)

        binding.toolbar.setupWithNavController(navController, appBarConfig)
        // Set nav view to use navigation component
        // This is only good if all navigation drawer open a fragment
        navView.setupWithNavController(navController)

        // Only overwrite this if wish to do other thing than navigation
        navView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            binding.drawerLayout.closeDrawers()
            when (menuItem.itemId) {
//                R.id.logout_menu -> {
//                    Toast.makeText(this, "Logout is clicked",
//                        Toast.LENGTH_SHORT).show()
//                    finish()
//                }
                else -> true
            }

            // Use this to ensure other menu still navigate to the correct fragment
            NavigationUI.onNavDestinationSelected(menuItem, navController)
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfig) || super.onSupportNavigateUp()
    }

}

