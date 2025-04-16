package com.example.budgetbee.fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.budgetbee.R
import com.example.budgetbee.databinding.FragmentSettingsBinding
import com.example.budgetbee.utils.DailyReminderReceiver
import com.example.budgetbee.utils.FileHelper
import com.example.budgetbee.utils.SharedPrefHelper
import com.google.android.material.snackbar.Snackbar
import java.util.*

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPrefHelper: SharedPrefHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        sharedPrefHelper = SharedPrefHelper(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCurrencySpinner()
        setupViews()
        setupListeners()
    }

    private fun setupCurrencySpinner() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.currencies,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCurrency.adapter = adapter
            val position = (adapter as ArrayAdapter<String>).getPosition(sharedPrefHelper.currency)
            binding.spinnerCurrency.setSelection(if (position >= 0) position else 0)
        }
    }

    private fun setupViews() {
        binding.apply {
            etBudget.setText(sharedPrefHelper.monthlyBudget.toString())
            switchDailyReminder.isChecked = sharedPrefHelper.dailyReminderEnabled
            btnExport.isEnabled = sharedPrefHelper.getTransactions().isNotEmpty()
            btnImport.isEnabled = FileHelper.backupExists(requireContext())
        }
    }

    private fun setupListeners() {
        binding.btnSaveSettings.setOnClickListener { saveSettings() }

        binding.btnExport.setOnClickListener {
            val transactions = sharedPrefHelper.getTransactions()
            when {
                transactions.isEmpty() -> showMessage("No transactions to export")
                FileHelper.exportToFile(requireContext(), transactions) -> showMessage("Backup saved successfully")
                else -> showMessage("Backup failed! Check storage")
            }
        }

        binding.btnImport.setOnClickListener {
            try {
                FileHelper.importFromFile(requireContext())?.let { transactions ->
                    sharedPrefHelper.saveTransactions(transactions)
                    showMessage("Restored ${transactions.size} transactions")
                    setupViews()
                } ?: showMessage("No backup file found")
            } catch (e: Exception) {
                showMessage("Invalid backup format: ${e.message}")
            }
        }

        binding.switchDailyReminder.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefHelper.dailyReminderEnabled = isChecked
            setupDailyReminder(isChecked)
        }
    }

    private fun saveSettings() {
        try {
            sharedPrefHelper.apply {
                monthlyBudget = binding.etBudget.text.toString().toDouble()
                currency = binding.spinnerCurrency.selectedItem.toString()
            }
            showMessage("Settings saved")
        } catch (e: NumberFormatException) {
            showMessage("Invalid budget amount")
        }
    }

    private fun setupDailyReminder(enabled: Boolean) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), DailyReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        if (enabled) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 20)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (before(Calendar.getInstance())) add(Calendar.DATE, 1)
            }

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } else {
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}