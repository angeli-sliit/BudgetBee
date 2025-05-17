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
import com.example.budgetbee.viewModel.UserViewModel
import androidx.activity.viewModels
import com.example.budgetbee.utils.NotificationHelper

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Permission granted, create notification channels
                NotificationHelper.createNotificationChannels(this)
            }
        }
    }

    private fun setupNavigation() {
        val navView: BottomNavigationView = binding.bottomNav
        val navController = findNavController(R.id.nav_host_fragment)

        // Check if user is logged in only when app starts
        viewModel.currentUser.observe(this) { user ->
            if (user == null && navController.currentDestination?.id != R.id.loginFragment) {
                navController.navigate(R.id.loginFragment)
            }
        }

        // Setup navigation with animation disabled for better performance
        navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Disable animations for smoother transitions
            navView.animate().setDuration(0).start()
        }

        // Optional: Add badge for budget warnings
        navView.getOrCreateBadge(R.id.dashboardFragment).apply {
            backgroundColor = ContextCompat.getColor(this@MainActivity, R.color.expense_color)
            isVisible = false
        }
    }
}
