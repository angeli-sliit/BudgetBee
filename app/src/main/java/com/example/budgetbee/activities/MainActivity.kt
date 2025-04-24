package com.example.budgetbee.activities

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.budgetbee.R
import com.example.budgetbee.databinding.ActivityMainBinding
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                100
            )
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navView: BottomNavigationView = binding.bottomNav
        val navController = findNavController(R.id.nav_host_fragment)

        // Set up the bottom navigation with the nav controller
        navView.setupWithNavController(navController)

        // Optional: Add badge for budget warnings
        navView.getOrCreateBadge(R.id.navigation_dashboard).apply {
            backgroundColor = ContextCompat.getColor(this@MainActivity, R.color.expense_color)
            isVisible = false
        }

        // Handle navigation item selection
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    navController.navigate(R.id.dashboardFragment)
                    true
                }
                R.id.navigation_transactions -> {
                    navController.navigate(R.id.transactionsFragment)
                    true
                }
                R.id.navigation_add -> {
                    navController.navigate(R.id.addTransactionFragment)
                    true
                }
                R.id.navigation_budget -> {
                    navController.navigate(R.id.budgetFragment)
                    true
                }
                R.id.navigation_settings -> {
                    navController.navigate(R.id.settingsFragment)
                    true
                }
                else -> false
            }
        }
    }
}