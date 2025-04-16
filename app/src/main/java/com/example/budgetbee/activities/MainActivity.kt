package com.example.budgetbee.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.budgetbee.R
import com.example.budgetbee.databinding.ActivityMainBinding
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navView: BottomNavigationView = binding.bottomNav
        val navController = findNavController(R.id.nav_host_fragment)

        navView.setupWithNavController(navController)

        // Optional: Add badge for budget warnings
        navView.getOrCreateBadge(R.id.dashboardFragment).apply {
            backgroundColor = resources.getColor(R.color.expense_color)
            isVisible = false
        }

    }


}