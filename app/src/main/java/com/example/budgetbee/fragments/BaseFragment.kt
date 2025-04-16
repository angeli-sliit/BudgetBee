package com.example.budgetbee.fragments

import android.content.Context
import androidx.fragment.app.Fragment
import com.example.budgetbee.utils.SharedPrefHelper

abstract class BaseFragment : Fragment() {
    protected lateinit var sharedPrefHelper: SharedPrefHelper

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPrefHelper = SharedPrefHelper(context) // Remove requireContext() here
    }

    protected fun registerForUpdates() {
        sharedPrefHelper.sharedPref.registerOnSharedPreferenceChangeListener { _, key ->
            if (key == "transactions" || key == "monthlyBudget") {
                updateUI()
            }
        }
    }

    abstract fun updateUI()
}